package v1.foodDeliveryPlatform.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessagingException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.service.EmailService;
import v1.foodDeliveryPlatform.service.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment acknowledgment;

    @Captor
    private ArgumentCaptor<Properties> propertiesCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private OrderEventConsumer orderEventConsumer;
    private MeterRegistry meterRegistry;

    private final String validOrderEventJson = """
            {
                "eventId": "test-event-id",
                "orderId": "test-order-id",
                "userId": "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName": "Test Restaurant",
                "totalAmount": 1500.50,
                "items": [
                    {
                        "dishName": "Test Dish 1",
                        "quantity": 2,
                        "price": 500.25
                    },
                    {
                        "dishName": "Test Dish 2",
                        "quantity": 1,
                        "price": 500.00
                    }
                ]
            }
            """;

    private final User testUser = User.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .email("test@example.com")
            .name("Test User")
            .build();

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        orderEventConsumer = new OrderEventConsumer(emailService, userService, objectMapper, meterRegistry);
    }

    @Test
    void handleOrderCompleted_Success_ShouldProcessEventAndSendEmail() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", 1500.50,
                "items", List.of(
                        Map.of("dishName", "Test Dish 1", "quantity", 2, "price", 500.25),
                        Map.of("dishName", "Test Dish 2", "quantity", 1, "price", 500.00)
                )
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(objectMapper).readValue(eq(validOrderEventJson), any(TypeReference.class));
        verify(userService).getById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        verify(emailService).sendEmail(userCaptor.capture(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());
        verify(acknowledgment).acknowledge();

        User capturedUser = userCaptor.getValue();
        Properties capturedProperties = propertiesCaptor.getValue();

        assertEquals(testUser.getEmail(), capturedUser.getEmail());
        assertEquals("test-order-id", capturedProperties.getProperty("orderId"));
        assertEquals("1500.5", capturedProperties.getProperty("totalAmount"));
        assertEquals("Test Restaurant", capturedProperties.getProperty("restaurantName"));

        String itemsText = capturedProperties.getProperty("items");
        assertTrue(itemsText.contains("Test Dish 1 × 2 - 500.25 ₽"));
        assertTrue(itemsText.contains("Test Dish 2 × 1 - 500.0 ₽"));
    }

    @Test
    void handleOrderCompleted_JsonParsingError_ShouldThrowException() throws Exception {

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {
                });

        assertThrows(JsonProcessingException.class, () ->
                orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void handleOrderCompleted_UserNotFound_ShouldThrowException() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", 1500.50,
                "items", List.of()
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void handleOrderCompleted_EmailSendingFailed_ShouldThrowException() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", 1500.50,
                "items", List.of()
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);
        doThrow(new MessagingException("Email sending failed"))
                .when(emailService).sendEmail(any(), any(), any());

        assertThrows(MessagingException.class, () ->
                orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void handleOrderCompleted_WithNullItems_ShouldFormatCorrectly() throws Exception {

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "test-event-id");
        event.put("orderId", "test-order-id");
        event.put("userId", "123e4567-e89b-12d3-a456-426614174000");
        event.put("restaurantName", "Test Restaurant");
        event.put("totalAmount", 1000.00);
        event.put("items", null);

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(emailService).sendEmail(any(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());

        Properties capturedProperties = propertiesCaptor.getValue();
        assertEquals("No items", capturedProperties.getProperty("items"));
    }

    @Test
    void handleOrderCompleted_WithEmptyItems_ShouldFormatCorrectly() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", 1000.00,
                "items", List.of()
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(emailService).sendEmail(any(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());

        Properties capturedProperties = propertiesCaptor.getValue();
        assertEquals("No items", capturedProperties.getProperty("items"));
    }

    @Test
    void handleOrderCompleted_WithDifferentNumberTypes_ShouldConvertCorrectly() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", 1000, // Integer
                "items", List.of(
                        Map.of("dishName", "Test Dish", "quantity", 2L, "price", 500.50)
                )
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(emailService).sendEmail(any(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());
        Properties props = propertiesCaptor.getValue();
        assertEquals("1000.0", props.getProperty("totalAmount"));
        assertTrue(props.getProperty("items").contains("500.5"));
    }

    @Test
    void handleOrderCompleted_WithStringNumbers_ShouldConvertCorrectly() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", "1500.75",
                "items", List.of(
                        Map.of("dishName", "Test Dish", "quantity", "2", "price", "500.25")
                )
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(objectMapper).readValue(eq(validOrderEventJson), any(TypeReference.class));

        ArgumentCaptor<TypeReference<Map<String, Object>>> typeRefCaptor = ArgumentCaptor.forClass(TypeReference.class);
        verify(objectMapper).readValue(eq(validOrderEventJson), typeRefCaptor.capture());

        verify(emailService).sendEmail(any(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());
        Properties props = propertiesCaptor.getValue();

        assertEquals("1500.75", props.getProperty("totalAmount"));

        String itemsText = props.getProperty("items");
        System.out.println("=== DEBUG INFO ===");
        System.out.println("Expected event: " + event);
        System.out.println("Actual items text: " + itemsText);
        System.out.println("=== END DEBUG ===");

        assertTrue(itemsText.contains("Test Dish"), "Should contain dish name");
        assertTrue(itemsText.contains("500.25"), "Should contain price");
        assertTrue(itemsText.contains("₽"), "Should contain currency symbol");
    }

    @Test
    void handleOrderCompleted_WithMissingItemData_ShouldFormatGracefully() throws Exception {

        Map<String, Object> event = Map.of(
                "eventId", "test-event-id",
                "orderId", "test-order-id",
                "userId", "123e4567-e89b-12d3-a456-426614174000",
                "restaurantName", "Test Restaurant",
                "totalAmount", 1000.00,
                "items", List.of(
                        Map.of("dishName", "Valid Dish", "quantity", 2, "price", 500.25),
                        Map.of("quantity", 1, "price", 300.00),
                        Map.of("dishName", "No Quantity Dish", "price", 200.00),
                        Map.of("dishName", "No Price Dish", "quantity", 3)
                )
        );

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(emailService).sendEmail(any(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());
        String itemsText = propertiesCaptor.getValue().getProperty("items");

        assertTrue(itemsText.contains("Valid Dish × 2 - 500.25 ₽"));
        assertTrue(itemsText.contains("Unknown Dish × 1 - 300.0 ₽"));
        assertTrue(itemsText.contains("No Quantity Dish × 1 - 200.0 ₽"));
        assertTrue(itemsText.contains("No Price Dish × 3 - 0 ₽"));
    }

    @Test
    void handleOrderCompleted_WithNullTotalAmount_ShouldUseZero() throws Exception {

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "test-event-id");
        event.put("orderId", "test-order-id");
        event.put("userId", "123e4567-e89b-12d3-a456-426614174000");
        event.put("restaurantName", "Test Restaurant");
        event.put("totalAmount", null);
        event.put("items", List.of());

        when(objectMapper.readValue(eq(validOrderEventJson), any(TypeReference.class)))
                .thenReturn(event);
        when(userService.getById(any(UUID.class))).thenReturn(testUser);

        orderEventConsumer.handleOrderCompleted(validOrderEventJson, "test-key", 0, 1L, acknowledgment);

        verify(emailService).sendEmail(any(), eq(MailType.ORDER_RECEIPT), propertiesCaptor.capture());
        assertEquals("0", propertiesCaptor.getValue().getProperty("totalAmount"));
    }
}
