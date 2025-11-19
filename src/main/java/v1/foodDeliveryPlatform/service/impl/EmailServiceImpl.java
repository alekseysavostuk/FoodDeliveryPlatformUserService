package v1.foodDeliveryPlatform.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.service.EmailService;

import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(User user, MailType type, Properties params) throws MessagingException {
        log.info("Preparing to send {} email to user: {} ({})", type, user.getEmail(), user.getId());

        switch (type) {
            case REGISTRATION -> sendRegistrationEmail(user, params);
            case ORDER_RECEIPT -> sendOrderReceiptEmail(user, params);
            default -> log.warn("Unknown mail type: {} for user: {}", type, user.getEmail());
        }

        log.debug("Email type {} processing completed for user: {}", type, user.getEmail());
    }

    private void sendRegistrationEmail(User user, Properties params) throws MessagingException {
        String userEmail = user.getEmail();
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email is null or empty");
        }

        if (!isValidEmail(userEmail)) {
            throw new IllegalArgumentException("Invalid email format: " + userEmail);
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Welcome to the club, buddy " + user.getName());
        helper.setTo(user.getEmail());
        String emailContent = getRegistrationEmailContent(user, params);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    private void sendOrderReceiptEmail(User user, Properties params) throws MessagingException {
        String orderId = params.getProperty("orderId");
        log.debug("Starting order receipt email for order: {}, user: {}", orderId, user.getEmail());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setSubject("Order receipt #" + orderId);
            helper.setTo(user.getEmail());
            String emailContent = getOrderReceiptEmailContent(user, params);
            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);
            log.info("Order receipt email successfully sent for order: {} to: {}", orderId, user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send order receipt email for order: {} to: {}", orderId, user.getEmail(), e);
            throw e;
        }
    }

    private String getOrderReceiptEmailContent(User user, Properties params) {
        log.trace("Generating order receipt email content for user: {}, order: {}",
                user.getEmail(), params.getProperty("orderId"));

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderId", params.getProperty("orderId"));
        context.setVariable("totalAmount", params.getProperty("totalAmount"));
        context.setVariable("restaurantName", params.getProperty("restaurantName"));
        context.setVariable("items", params.getProperty("items"));

        String content = templateEngine.process("order-receipt", context);
        log.trace("Order receipt email content generated successfully");
        return content;
    }

    private String getRegistrationEmailContent(User user, Properties params) {
        log.trace("Generating registration email content for user: {}", user.getEmail());

        Context context = new Context();
        context.setVariable("name", user.getName());

        String content = templateEngine.process("register", context);
        log.trace("Registration email content generated successfully");
        return content;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        boolean isValid = email != null && email.matches(emailRegex);

        if (!isValid) {
            log.debug("Email validation failed for: {}", email);
        }

        return isValid;
    }
}
