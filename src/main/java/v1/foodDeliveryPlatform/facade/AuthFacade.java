package v1.foodDeliveryPlatform.facade;

import v1.foodDeliveryPlatform.dto.auth.AccessTokenDto;
import v1.foodDeliveryPlatform.dto.auth.AuthDto;
import v1.foodDeliveryPlatform.dto.model.UserDto;

public interface AuthFacade {
    AccessTokenDto getToken(AuthDto authDto);
    void saveUser(UserDto userDto);
}
