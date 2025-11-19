package v1.foodDeliveryPlatform.facade;

import jakarta.mail.MessagingException;
import v1.foodDeliveryPlatform.dto.auth.JwtRequest;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.dto.auth.RefreshTokenRequest;
import v1.foodDeliveryPlatform.dto.model.UserDto;

public interface AuthFacade {
    JwtResponse getToken(JwtRequest jwtRequest);

    void createUser(UserDto userDto) throws MessagingException;

    JwtResponse refreshToken(RefreshTokenRequest refreshToken);

    void confirmEmail(String email, String code);
}
