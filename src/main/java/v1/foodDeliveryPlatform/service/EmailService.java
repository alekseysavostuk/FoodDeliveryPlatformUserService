package v1.foodDeliveryPlatform.service;

import jakarta.mail.MessagingException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;

import java.util.Properties;

public interface EmailService {
    public void sendEmail(User user, MailType type, Properties params) throws MessagingException;

    void confirmEmail(String email, String confirmationCode);
}
