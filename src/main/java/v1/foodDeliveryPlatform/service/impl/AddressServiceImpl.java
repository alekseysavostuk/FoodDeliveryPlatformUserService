package v1.foodDeliveryPlatform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
import v1.foodDeliveryPlatform.model.Address;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.AddressRepository;
import v1.foodDeliveryPlatform.service.AddressService;
import v1.foodDeliveryPlatform.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Override
    @Transactional
    @Cacheable(value = "addresses", key = "#id")
    public Address getById(UUID id) {
        log.debug("Fetching address by ID: {}", id);
        Address address = addressRepository.findById(id).orElseThrow(() -> {
            log.warn("Address not found with ID: {}", id);
            return new ResourceNotFoundException("Address not found");
        });
        log.debug("Successfully fetched address: {} ({})", address.getCity(), address.getId());
        return address;
    }

    @Override
    @Transactional
    public Address createAddress(Address address, UUID userId) {
        log.info("Creating new address for user ID: {}", userId);

        User user = userService.getById(userId);
        address.setUser(user);

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully: {} ({}) for user: {}",
                savedAddress.getCity(), savedAddress.getId(), userId);

        return savedAddress;
    }

    @Override
    @Transactional
    @Cacheable(value = "user_addresses", key = "#userId")
    public List<Address> getAllByUserId(UUID userId) {
        log.debug("Fetching all addresses for user ID: {}", userId);

        List<Address> addresses = addressRepository.findAllByUserId(userId);
        log.debug("Found {} addresses for user ID: {}", addresses.size(), userId);

        return addresses;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "addresses", key = "#address.id"),
            @CacheEvict(value = "user_addresses", key = "#result.user.id")
    })
    public Address updateAddress(Address address) {
        log.info("Updating address with ID: {}", address.getId());

        Address currentAddress = getById(address.getId());

        log.debug("Address update - city: {}, street: {}, country: {}",
                address.getCity(), address.getStreet(), address.getCountry());

        currentAddress.setCity(address.getCity());
        currentAddress.setStreet(address.getStreet());
        currentAddress.setState(address.getState());
        currentAddress.setZip(address.getZip());
        currentAddress.setCountry(address.getCountry());

        Address updatedAddress = addressRepository.save(currentAddress);
        log.info("Address updated successfully: {} ({})",
                updatedAddress.getCity(), updatedAddress.getId());

        return updatedAddress;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "addresses", key = "#id"),
            @CacheEvict(value = "user_addresses", allEntries = true)
    })
    public void delete(UUID id) {
        log.info("Deleting address with ID: {}", id);

        try {
            addressRepository.deleteDirectlyById(id);
            log.info("Address deleted successfully: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete address with ID: {}", id, e);
            throw e;
        }
    }

}
