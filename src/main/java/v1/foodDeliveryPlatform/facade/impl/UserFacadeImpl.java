package v1.foodDeliveryPlatform.facade.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import v1.foodDeliveryPlatform.dto.auth.ChangePasswordRequest;
import v1.foodDeliveryPlatform.dto.auth.PasswordConfirm;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.UserFacade;
import v1.foodDeliveryPlatform.mapper.UserMapper;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.service.AuthService;
import v1.foodDeliveryPlatform.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper mapper;

    @Override
    public UserDto getById(UUID id) {
        return mapper.toDto(userService.getById(id));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        return mapper.toDto(userService.updateUser(mapper.toEntity(userDto)));
    }

    @Override
    public void delete(UUID id, PasswordConfirm passwordConfirm) {
        authService.authenticate(getById(id).getEmail(), passwordConfirm.getPassword());
        userService.delete(id);
    }

    @Override
    public void delete(UUID id) {
        userService.delete(id);
    }

    @Override
    public UserDto updateRole(UUID id) {
        return mapper.toDto(userService.updateRole(id));
    }

    @Override
    public UserDto changePassword(UUID id, ChangePasswordRequest request) {
        authService.authenticate(request.getEmail(), request.getOldPassword());
        return mapper.toDto(userService.changePassword(id, request.getNewPassword()));
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
