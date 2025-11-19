package v1.foodDeliveryPlatform.facade.impl;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import v1.foodDeliveryPlatform.dto.auth.JwtRequest;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.dto.auth.RefreshTokenRequest;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.AuthFacade;
import v1.foodDeliveryPlatform.mapper.UserMapper;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.service.AuthService;
import v1.foodDeliveryPlatform.service.EmailService;

import java.util.Properties;

@Component
@AllArgsConstructor
public class AuthFacadeImpl implements AuthFacade {

    private final AuthService authService;
    private final UserMapper mapper;
    private final EmailService emailService;

    @Override
    public JwtResponse getToken(JwtRequest jwtRequest) {
        return authService.loginWithEmailAndPassword(jwtRequest.getEmail(), jwtRequest.getPassword());
    }

    @Override
    public void createUser(UserDto userDto) throws MessagingException {
        User user = authService.createUser(mapper.toEntity(userDto));
        emailService.sendEmail(user, MailType.REGISTRATION, new Properties());
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest refreshToken) {
        return authService.refresh(refreshToken.getRefreshToken());
    }

    @Override
    public void confirmEmail(String email, String code) {
        emailService.confirmEmail(email, code);
    }
}
