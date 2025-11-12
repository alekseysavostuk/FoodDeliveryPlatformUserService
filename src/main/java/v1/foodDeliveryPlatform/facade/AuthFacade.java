package v1.foodDeliveryPlatform.facade;

import v1.foodDeliveryPlatform.dto.auth.JwtRequest;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.dto.auth.RefreshTokenRequest;
import v1.foodDeliveryPlatform.dto.model.UserDto;

public interface AuthFacade {
    JwtResponse getToken(JwtRequest jwtRequest);

    UserDto createUser(UserDto userDto);

    JwtResponse refreshToken(RefreshTokenRequest refreshToken);
}
