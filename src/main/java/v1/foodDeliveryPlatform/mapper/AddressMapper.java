package v1.foodDeliveryPlatform.mapper;

import org.mapstruct.Mapper;
import v1.foodDeliveryPlatform.dto.model.AddressDto;
import v1.foodDeliveryPlatform.model.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper extends BaseMapper<Address, AddressDto> {
}
