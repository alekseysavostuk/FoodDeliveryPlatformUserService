package v1.foodDeliveryPlatform.facade;

import v1.foodDeliveryPlatform.dto.model.AddressDto;

import java.util.List;
import java.util.UUID;

public interface AddressFacade {
    AddressDto getById(UUID id);

    AddressDto createAddress(AddressDto addressDto, UUID userId);

    List<AddressDto> getAllByUserId(UUID userId);

    AddressDto updateAddress(AddressDto addressDto);

    void delete(UUID id);
}
