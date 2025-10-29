package v1.foodDeliveryPlatform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import v1.foodDeliveryPlatform.dto.auth.AuthDto;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "name", source = "name")
    User toEntity(UserDto userDto);

}
