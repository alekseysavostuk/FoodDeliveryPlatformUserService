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
import v1.foodDeliveryPlatform.service.AuthService;

@Component
@AllArgsConstructor
public class AuthFacadeImpl implements AuthFacade {

    private final AuthService authService;
    private final UserMapper mapper;

    @Override
    public JwtResponse getToken(JwtRequest jwtRequest) {
        return authService.loginWithEmailAndPassword(jwtRequest.getEmail(), jwtRequest.getPassword());
    }

    @Override
    public UserDto createUser(UserDto userDto) throws MessagingException {
        return mapper.toDto(authService.createUser(mapper.toEntity(userDto)));
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest refreshToken) {
        return authService.refresh(refreshToken.getRefreshToken());
    }
}
