package v1.foodDeliveryPlatform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.model.User;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper extends BaseMapper<User, UserDto> {

    @Override
    @Mapping(target = "addressDtoList", source = "addressList")
    @Mapping(target = "addressDtoList.user", ignore = true)
    UserDto toDto(User user);

    @Override
    @Mapping(target = "addressList", source = "addressDtoList")
    @Mapping(target = "addressDtoList.user", ignore = true)
    User toEntity(UserDto userDto);
}

