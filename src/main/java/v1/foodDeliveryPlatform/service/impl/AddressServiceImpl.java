package v1.foodDeliveryPlatform.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.exception.ModelExistsException;
import v1.foodDeliveryPlatform.model.Address;
import v1.foodDeliveryPlatform.repository.AddressRepository;
import v1.foodDeliveryPlatform.service.AddressService;
import v1.foodDeliveryPlatform.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Override
    public Address getById(UUID id) {
        return addressRepository.findById(id).orElseThrow(() ->
                new ModelExistsException("Address not found"));
    }

    @Override
    public Address createAddress(Address address, UUID userId) {
        address.setUser(userService.getById(userId));
        addressRepository.save(address);
        return address;
    }

    @Override
    @Transactional
    public List<Address> getAllByUserId(UUID userId) {
        return addressRepository.findAllByUserId(userId);
    }

    @Override
    public Address updateAddress(Address address) {
        Address currentAddress = getById(address.getId());
        currentAddress.setCity(address.getCity());
        currentAddress.setStreet(address.getStreet());
        currentAddress.setState(address.getState());
        currentAddress.setZip(address.getZip());
        currentAddress.setCountry(address.getCountry());
        addressRepository.save(currentAddress);
        return currentAddress;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        addressRepository.deleteDirectlyById(id);
    }
}
