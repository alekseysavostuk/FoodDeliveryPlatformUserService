package v1.foodDeliveryPlatform.facade;

import v1.foodDeliveryPlatform.dto.model.UserDto;

import java.util.UUID;

public interface UserFacade {
    UserDto getById(UUID id);

    UserDto updateUser(UserDto userDto);

    void delete(UUID id);

    UserDto updateRole(UUID id);
}
