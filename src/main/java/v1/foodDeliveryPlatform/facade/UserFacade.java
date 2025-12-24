package v1.foodDeliveryPlatform.facade;

import v1.foodDeliveryPlatform.dto.auth.ChangePasswordRequest;
import v1.foodDeliveryPlatform.dto.auth.PasswordConfirm;
import v1.foodDeliveryPlatform.dto.model.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserFacade {

    UserDto getById(UUID id);

    UserDto updateUser(UserDto userDto);

    void delete(UUID id, PasswordConfirm passwordConfirm);

    void delete(UUID id);

    UserDto updateRole(UUID id);

    UserDto changePassword(UUID id, ChangePasswordRequest changePasswordRequest);

    List<UserDto> getAllUsers();
}
