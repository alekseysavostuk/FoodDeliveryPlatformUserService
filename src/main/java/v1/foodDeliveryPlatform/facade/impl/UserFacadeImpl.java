package v1.foodDeliveryPlatform.facade.impl;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import v1.foodDeliveryPlatform.dto.auth.ChangePasswordRequest;
import v1.foodDeliveryPlatform.dto.auth.PasswordConfirm;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.UserFacade;
import v1.foodDeliveryPlatform.mapper.UserMapper;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.service.AuthService;
import v1.foodDeliveryPlatform.service.EmailService;
import v1.foodDeliveryPlatform.service.UserService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper mapper;
    private final EmailService emailService;

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
    public void changePassword(UUID id, ChangePasswordRequest request) throws MessagingException {
        authService.authenticate(request.getEmail(), request.getOldPassword());
        User user = userService.changePassword(id, request.getNewPassword());
        emailService.sendEmail(user, MailType.CHANGE_PASSWORD, new Properties());
    }


    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void confirmPasswordChange(String email, String code) {
        emailService.confirmPasswordChange(email, code);
    }
}
