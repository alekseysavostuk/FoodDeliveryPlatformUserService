package v1.foodDeliveryPlatform.facade.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import v1.foodDeliveryPlatform.dto.auth.AccessTokenDto;
import v1.foodDeliveryPlatform.dto.auth.AuthDto;
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
    public AccessTokenDto getToken(AuthDto authDto) {
        return new AccessTokenDto(authService.loginWithEmailAndPassword(authDto.getEmail(), authDto.getPassword()));
    }

    @Override
    public void saveUser(UserDto userDto) {
        authService.saveUser(mapper.toEntity(userDto));
    }
}
