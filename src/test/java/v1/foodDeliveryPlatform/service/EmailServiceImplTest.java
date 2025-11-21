package v1.foodDeliveryPlatform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.service.impl.EmailServiceImpl;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserService userService;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private User testUser;
    private Properties testProperties;
    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(uuid);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setConfirmationCode("ABC123");

        testProperties = new Properties();
        testProperties.setProperty("orderId", "ORDER-123");
        testProperties.setProperty("totalAmount", "99.99");
        testProperties.setProperty("restaurantName", "Test Restaurant");
        testProperties.setProperty("items", "Pizza, Burger");
    }

    @Test
    @DisplayName("Should send registration email successfully")
    void sendEmail_Registration_Success() throws MessagingException {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email-confirmation"), any(Context.class)))
                .thenReturn("<html>Registration email content</html>");

        emailService.sendEmail(testUser, MailType.REGISTRATION, testProperties);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("email-confirmation"), any(Context.class));
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void sendEmail_Welcome_Success() throws MessagingException {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("register"), any(Context.class)))
                .thenReturn("<html>Welcome email content</html>");

        emailService.sendEmail(testUser, MailType.WELCOME, testProperties);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("register"), any(Context.class));
    }

    @Test
    @DisplayName("Should send order receipt email successfully")
    void sendEmail_OrderReceipt_Success() throws MessagingException {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("order-receipt"), any(Context.class)))
                .thenReturn("<html>Order receipt content</html>");

        emailService.sendEmail(testUser, MailType.ORDER_RECEIPT, testProperties);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("order-receipt"), any(Context.class));
    }

    @Test
    @DisplayName("Should log warning for unknown mail type")
    void sendEmail_UnknownType_ShouldLogWarning() {

        assertDoesNotThrow(() ->
                emailService.sendEmail(testUser, null, testProperties)
        );

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when registration email is null")
    void sendRegistrationEmail_NullEmail_ShouldThrowException() {

        testUser.setEmail(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendEmail(testUser, MailType.REGISTRATION, testProperties));

        assertEquals("User email is null or empty", exception.getMessage());

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when registration email is empty")
    void sendRegistrationEmail_EmptyEmail_ShouldThrowException() {

        testUser.setEmail("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendEmail(testUser, MailType.REGISTRATION, testProperties));

        assertEquals("User email is null or empty", exception.getMessage());

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when registration email is invalid")
    void sendRegistrationEmail_InvalidEmail_ShouldThrowException() {

        testUser.setEmail("invalid-email");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendEmail(testUser, MailType.REGISTRATION, testProperties));

        assertEquals("Invalid email format: invalid-email", exception.getMessage());

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should confirm email successfully with valid code")
    void confirmEmail_Success() throws MessagingException {

        User userToUpdate = new User();
        userToUpdate.setId(uuid);
        userToUpdate.setEmail("test@example.com");
        userToUpdate.setConfirmationCode("ABC123");
        userToUpdate.setEmailConfirmed(false);

        when(userService.getByEmail("test@example.com")).thenReturn(userToUpdate);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("register"), any(Context.class)))
                .thenReturn("<html>Welcome email content</html>");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setEmailConfirmed(true);
            user.setConfirmationCode(null);
            return null;
        }).when(userService).updateUser(any(User.class));

        emailService.confirmEmail("test@example.com", "ABC123");

        verify(userService).getByEmail("test@example.com");
        verify(userService).updateUser(userToUpdate);
        assertTrue(userToUpdate.isEmailConfirmed());
        assertNull(userToUpdate.getConfirmationCode());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw exception when user not found during confirmation")
    void confirmEmail_UserNotFound_ShouldThrowException() {

        when(userService.getByEmail("nonexistent@example.com"))
                .thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.confirmEmail("nonexistent@example.com", "ABC123"));

        assertEquals("User not found", exception.getMessage());
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when confirmation code is invalid")
    void confirmEmail_InvalidCode_ShouldThrowException() {

        testUser.setConfirmationCode("CORRECT_CODE");
        when(userService.getByEmail("test@example.com")).thenReturn(testUser);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.confirmEmail("test@example.com", "WRONG_CODE"));

        assertEquals("Invalid confirmation code", exception.getMessage());
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Should handle welcome email failure gracefully during confirmation")
    void confirmEmail_WelcomeEmailFails_ShouldLogError() {

        testUser.setConfirmationCode("ABC123");
        testUser.setEmailConfirmed(false);

        when(userService.getByEmail("test@example.com")).thenReturn(testUser);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("register"), any(Context.class)))
                .thenReturn("<html>Welcome email content</html>");

        MessagingException messagingException = new MessagingException("SMTP error");
        doThrow(new RuntimeException(messagingException))
                .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() ->
                emailService.confirmEmail("test@example.com", "ABC123")
        );

        verify(userService).updateUser(testUser);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should propagate messaging exception when sending email fails")
    void sendOrderReceiptEmail_MessagingException_ShouldPropagate() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("order-receipt"), any(Context.class)))
                .thenReturn("<html>Order receipt content</html>");

        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendEmail(testUser, MailType.ORDER_RECEIPT, testProperties));

        assertEquals("SMTP error", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null properties gracefully")
    void sendEmail_NullProperties_ShouldWork() throws MessagingException {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("register"), any(Context.class)))
                .thenReturn("<html>Welcome email content</html>");

        emailService.sendEmail(testUser, MailType.WELCOME, null);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should handle template processing failure gracefully")
    void sendEmail_TemplateProcessingFails_ShouldPropagateException() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email-confirmation"), any(Context.class)))
                .thenThrow(new RuntimeException("Template error"));

        assertThrows(RuntimeException.class,
                () -> emailService.sendEmail(testUser, MailType.REGISTRATION, testProperties));
    }

    @Test
    @DisplayName("Should test email validation through public method")
    void emailValidation_ThroughPublicMethod() throws MessagingException {

        User validUser = new User();
        validUser.setEmail("valid@example.com");
        validUser.setConfirmationCode("CODE123");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email-confirmation"), any(Context.class)))
                .thenReturn("<html>Content</html>");

        assertDoesNotThrow(() ->
                emailService.sendEmail(validUser, MailType.REGISTRATION, testProperties)
        );

        User invalidUser = new User();
        invalidUser.setEmail("invalid-email");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendEmail(invalidUser, MailType.REGISTRATION, testProperties));

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }
}
