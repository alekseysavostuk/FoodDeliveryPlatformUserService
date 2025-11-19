package v1.foodDeliveryPlatform.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
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
    @Transactional
    @Cacheable(value = "addresses", key = "#id")
    public Address getById(UUID id) {
        return addressRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Address not found"));
    }

    @Override
    @Transactional
    public Address createAddress(Address address, UUID userId) {
        address.setUser(userService.getById(userId));
        addressRepository.save(address);
        return address;
    }

    @Override
    @Transactional
    @Cacheable(value = "user_addresses", key = "#userId")
    public List<Address> getAllByUserId(UUID userId) {
        return addressRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "addresses", key = "#address.id"),
            @CacheEvict(value = "user_addresses", key = "#result.user.id")
    })
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
    @Caching(evict = {
            @CacheEvict(value = "addresses", key = "#id"),
            @CacheEvict(value = "user_addresses", allEntries = true)
    })
    public void delete(UUID id) {
        addressRepository.deleteDirectlyById(id);
    }
}
