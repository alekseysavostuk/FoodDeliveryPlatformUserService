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
        switch (type) {
            case REGISTRATION -> sendRegistrationEmail(user, params);
            case ORDER_RECEIPT -> sendOrderReceiptEmail(user, params);
            default -> log.warn("Unknown mail type: {}", type);

        }
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
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Order receipt #" + params.getProperty("orderId"));
        helper.setTo(user.getEmail());
        String emailContent = getOrderReceiptEmailContent(user, params);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    private String getOrderReceiptEmailContent(User user, Properties params) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderId", params.getProperty("orderId"));
        context.setVariable("totalAmount", params.getProperty("totalAmount"));
        context.setVariable("restaurantName", params.getProperty("restaurantName"));
        context.setVariable("items", params.getProperty("items"));
        return templateEngine.process("order-receipt", context);
    }

    private String getRegistrationEmailContent(User user, Properties params) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        return templateEngine.process("register", context);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }
}
