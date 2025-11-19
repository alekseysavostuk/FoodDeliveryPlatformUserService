package v1.foodDeliveryPlatform.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.service.EmailService;
import v1.foodDeliveryPlatform.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final EmailService emailService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @KafkaListener(
            topics = "order-completed",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderCompleted(
            String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) throws MessagingException, JsonProcessingException {

        log.info("Received Kafka message - Topic: order-completed, Key: {}, Partition: {}, Offset: {}",
                key, partition, offset);
        log.debug("Raw message content: {}", message);

        Counter processedCounter = meterRegistry.counter("kafka.consumer.processed", "topic", "order-completed");
        Counter errorCounter = meterRegistry.counter("kafka.consumer.errors", "topic", "order-completed");
        Timer processingTimer = meterRegistry.timer("kafka.consumer.processing.duration", "topic", "order-completed");

        Timer.Sample sample = Timer.start(meterRegistry);
        String eventId = "unknown";

        log.debug("Parsing Kafka message to Map");

        try {
            Map<String, Object> event = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {
            });
            eventId = (String) event.get("eventId");

            log.info("Processing order event - EventId: {}, OrderId: {}, UserId: {}",
                    eventId, event.get("orderId"), event.get("userId"));

            String orderId = (String) event.get("orderId");
            String userId = (String) event.get("userId");
            String restaurantName = (String) event.get("restaurantName");
            BigDecimal totalAmount = convertToBigDecimal(event.get("totalAmount"));

            log.info("Getting user by ID: {}", userId);
            User user = userService.getById(UUID.fromString(userId));
            log.info("Found user: {} with email: {}", user.getName(), user.getEmail());

            log.info("Restaurant name from event: {}", restaurantName);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");
            String itemsText = formatItems(items);
            log.info("Formatted items: {}", itemsText);

            Properties params = new Properties();
            params.setProperty("orderId", orderId);
            params.setProperty("totalAmount", totalAmount.toString());
            params.setProperty("restaurantName", restaurantName);
            params.setProperty("items", itemsText);

            log.info("Attempting to send email to: {}", user.getEmail());
            emailService.sendEmail(user, MailType.ORDER_RECEIPT, params);
            log.info("Email sent successfully to: {}", user.getEmail());

            processedCounter.increment();
            ack.acknowledge();

            log.info("Order event processed successfully - EventId: {}, OrderId: {}", eventId, orderId);
        } catch (Exception e) {
            log.error("JSON parsing failed for event (EventId: {}): {}", eventId, e.getMessage());
            errorCounter.increment();
            meterRegistry.counter("kafka.consumer.failures",
                            "topic", "order-completed",
                            "error", e.getClass().getSimpleName())
                    .increment();
            throw e;
        } finally {
            sample.stop(processingTimer);
        }
    }

    private BigDecimal convertToBigDecimal(Object value) {
        log.trace("Converting value to BigDecimal: {}", value);
        return switch (value) {
            case null -> {
                log.trace("Null value converted to BigDecimal.ZERO");
                yield BigDecimal.ZERO;
            }
            case BigDecimal bigDecimal -> {
                log.trace("Value is already BigDecimal: {}", bigDecimal);
                yield bigDecimal;
            }
            case Number number -> {
                BigDecimal result = BigDecimal.valueOf(number.doubleValue());
                log.trace("Converted Number {} to BigDecimal: {}", number, result);
                yield result;
            }
            default -> {
                BigDecimal result = new BigDecimal(value.toString());
                log.trace("Converted String {} to BigDecimal: {}", value, result);
                yield result;
            }
        };
    }

    private String formatItems(List<Map<String, Object>> items) {
        log.trace("Formatting {} order items", items != null ? items.size() : 0);
        if (items == null || items.isEmpty()) {
            return "No items";
        }

        String formattedItems = items.stream()
                .map(item -> {
                    String dishName = (String) item.get("dishName");
                    if (dishName == null) {
                        dishName = "Unknown Dish";
                        log.warn("Missing dish name in order item");
                    }

                    Integer quantity = (item.get("quantity") instanceof Number) ?
                            ((Number) item.get("quantity")).intValue() : 1;
                    BigDecimal price = convertToBigDecimal(item.get("price"));

                    String itemString = String.format("%s × %d - %s ₽", dishName, quantity, price);
                    log.trace("Formatted item: {}", itemString);
                    return itemString;
                })
                .collect(Collectors.joining("\n"));

        log.debug("Successfully formatted {} items", items.size());
        return formattedItems;
    }
}
