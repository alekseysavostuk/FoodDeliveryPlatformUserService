package v1.foodDeliveryPlatform.facade.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.UserFacade;
import v1.foodDeliveryPlatform.mapper.UserMapper;
import v1.foodDeliveryPlatform.service.UserService;

import java.util.UUID;

@Component
@AllArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;
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
    public void delete(UUID id) {
        userService.delete(id);
    }

    @Override
    public UserDto updateRole(UUID id) {
        return mapper.toDto(userService.updateRole(id));
    }
}
