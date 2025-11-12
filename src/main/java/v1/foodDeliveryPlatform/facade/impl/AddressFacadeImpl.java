package v1.foodDeliveryPlatform.facade.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import v1.foodDeliveryPlatform.dto.model.AddressDto;
import v1.foodDeliveryPlatform.facade.AddressFacade;
import v1.foodDeliveryPlatform.mapper.AddressMapper;
import v1.foodDeliveryPlatform.model.Address;
import v1.foodDeliveryPlatform.service.AddressService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class AddressFacadeImpl implements AddressFacade {

    private final AddressService addressService;
    private final AddressMapper mapper;

    @Override
    public AddressDto getById(UUID id) {
        return mapper.toDto(addressService.getById(id));
    }

    @Override
    public AddressDto createAddress(AddressDto addressDto, UUID userId) {
        return mapper.toDto(addressService.createAddress(mapper.toEntity(addressDto), userId));
    }

    @Override
    public List<AddressDto> getAllByUserId(UUID userId) {
        List<Address> addresses = addressService.getAllByUserId(userId);
        return addresses.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public AddressDto updateAddress(AddressDto addressDto) {
        return mapper.toDto(addressService.updateAddress(mapper.toEntity(addressDto)));
    }

    @Override
    public void delete(UUID id) {
        addressService.delete(id);
    }
}
