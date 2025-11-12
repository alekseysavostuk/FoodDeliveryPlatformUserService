package v1.foodDeliveryPlatform.service;

import v1.foodDeliveryPlatform.model.Address;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    Address getById(UUID id);

    Address createAddress(Address address, UUID userId);

    List<Address> getAllByUserId(UUID userId);

    Address updateAddress(Address address);

    void delete(UUID id);
}
