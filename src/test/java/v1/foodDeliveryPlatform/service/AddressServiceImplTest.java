package v1.foodDeliveryPlatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
import v1.foodDeliveryPlatform.model.Address;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.AddressRepository;
import v1.foodDeliveryPlatform.service.impl.AddressServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private UUID testUserId;
    private UUID testAddressId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAddressId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testAddress = new Address();
        testAddress.setId(testAddressId);
        testAddress.setCity("Warsaw");
        testAddress.setStreet("Test Street 123");
        testAddress.setState("Mazovia");
        testAddress.setZip("00-001");
        testAddress.setCountry("Poland");
        testAddress.setUser(testUser);
    }

    @Test
    @DisplayName("Should get address by ID successfully")
    void getById_Success() {

        when(addressRepository.findById(testAddressId)).thenReturn(Optional.of(testAddress));


        Address result = addressService.getById(testAddressId);


        assertNotNull(result);
        assertEquals(testAddressId, result.getId());
        assertEquals("Warsaw", result.getCity());
        assertEquals("Test Street 123", result.getStreet());

        verify(addressRepository).findById(testAddressId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when address not found")
    void getById_NotFound() {

        when(addressRepository.findById(testAddressId)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> addressService.getById(testAddressId));

        assertEquals("Address not found", exception.getMessage());
        verify(addressRepository).findById(testAddressId);
    }

    @Test
    @DisplayName("Should create address successfully for valid user")
    void createAddress_Success() {

        Address newAddress = new Address();
        newAddress.setCity("Krakow");
        newAddress.setStreet("New Street 456");
        newAddress.setState("Lesser Poland");
        newAddress.setZip("30-001");
        newAddress.setCountry("Poland");

        Address savedAddress = new Address();
        savedAddress.setId(UUID.randomUUID());
        savedAddress.setCity("Krakow");
        savedAddress.setStreet("New Street 456");
        savedAddress.setState("Lesser Poland");
        savedAddress.setZip("30-001");
        savedAddress.setCountry("Poland");
        savedAddress.setUser(testUser);

        when(userService.getById(testUserId)).thenReturn(testUser);
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);


        Address result = addressService.createAddress(newAddress, testUserId);


        assertNotNull(result);
        assertEquals(savedAddress.getId(), result.getId());
        assertEquals("Krakow", result.getCity());
        assertEquals(testUser, result.getUser());

        verify(userService).getById(testUserId);
        verify(addressRepository).save(newAddress);
        assertEquals(testUser, newAddress.getUser());
    }

    @Test
    @DisplayName("Should get all addresses by user ID successfully")
    void getAllByUserId_Success() {

        List<Address> expectedAddresses = Arrays.asList(
                createTestAddress("Warsaw", "Street 1"),
                createTestAddress("Krakow", "Street 2"),
                createTestAddress("Gdansk", "Street 3")
        );

        when(addressRepository.findAllByUserId(testUserId)).thenReturn(expectedAddresses);


        List<Address> result = addressService.getAllByUserId(testUserId);


        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedAddresses, result);

        verify(addressRepository).findAllByUserId(testUserId);
    }

    @Test
    @DisplayName("Should return empty list when user has no addresses")
    void getAllByUserId_EmptyList() {

        when(addressRepository.findAllByUserId(testUserId)).thenReturn(Collections.emptyList());


        List<Address> result = addressService.getAllByUserId(testUserId);


        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(addressRepository).findAllByUserId(testUserId);
    }

    @Test
    @DisplayName("Should update address successfully")
    void updateAddress_Success() {

        Address updateRequest = new Address();
        updateRequest.setId(testAddressId);
        updateRequest.setCity("Updated City");
        updateRequest.setStreet("Updated Street");
        updateRequest.setState("Updated State");
        updateRequest.setZip("99-999");
        updateRequest.setCountry("Updated Country");

        Address existingAddress = new Address();
        existingAddress.setId(testAddressId);
        existingAddress.setCity("Old City");
        existingAddress.setStreet("Old Street");
        existingAddress.setState("Old State");
        existingAddress.setZip("00-000");
        existingAddress.setCountry("Old Country");
        existingAddress.setUser(testUser);

        Address savedAddress = new Address();
        savedAddress.setId(testAddressId);
        savedAddress.setCity("Updated City");
        savedAddress.setStreet("Updated Street");
        savedAddress.setState("Updated State");
        savedAddress.setZip("99-999");
        savedAddress.setCountry("Updated Country");
        savedAddress.setUser(testUser);


        when(addressRepository.findById(testAddressId)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.save(existingAddress)).thenReturn(savedAddress);


        Address result = addressService.updateAddress(updateRequest);


        assertNotNull(result);
        assertEquals("Updated City", result.getCity());
        assertEquals("Updated Street", result.getStreet());
        assertEquals("Updated State", result.getState());
        assertEquals("99-999", result.getZip());
        assertEquals("Updated Country", result.getCountry());


        assertEquals("Updated City", existingAddress.getCity());
        assertEquals("Updated Street", existingAddress.getStreet());
        assertEquals("Updated State", existingAddress.getState());
        assertEquals("99-999", existingAddress.getZip());
        assertEquals("Updated Country", existingAddress.getCountry());

        verify(addressRepository).findById(testAddressId);
        verify(addressRepository).save(existingAddress);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent address")
    void updateAddress_NotFound() {

        Address updateRequest = new Address();
        updateRequest.setId(testAddressId);

        when(addressRepository.findById(testAddressId)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> addressService.updateAddress(updateRequest));

        assertEquals("Address not found", exception.getMessage());

        verify(addressRepository).findById(testAddressId);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    @DisplayName("Should delete address successfully")
    void delete_Success() {

        doNothing().when(addressRepository).deleteDirectlyById(testAddressId);

        addressService.delete(testAddressId);

        verify(addressRepository).deleteDirectlyById(testAddressId);
    }

    @Test
    @DisplayName("Should propagate exception when delete fails")
    void delete_Failure() {

        doThrow(new RuntimeException("Database error"))
                .when(addressRepository)
                .deleteDirectlyById(testAddressId);


        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> addressService.delete(testAddressId));

        assertEquals("Database error", exception.getMessage());

        verify(addressRepository).deleteDirectlyById(testAddressId);
    }

    @Test
    @DisplayName("Should handle batch operations correctly")
    void batchOperations_Success() {

        List<Address> addresses = Arrays.asList(
                createTestAddress("Warsaw", "Street A"),
                createTestAddress("Krakow", "Street B"),
                createTestAddress("Gdansk", "Street C")
        );

        when(addressRepository.findAllByUserId(testUserId)).thenReturn(addresses);

        List<Address> result = addressService.getAllByUserId(testUserId);

        assertEquals(3, result.size());
        List<String> cities = result.stream()
                .map(Address::getCity)
                .collect(Collectors.toList());
        assertTrue(cities.containsAll(Arrays.asList("Warsaw", "Krakow", "Gdansk")));

        verify(addressRepository).findAllByUserId(testUserId);
    }

    private Address createTestAddress(String city, String street) {
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setCity(city);
        address.setStreet(street);
        address.setState("Test State");
        address.setZip("00-000");
        address.setCountry("Poland");
        address.setUser(testUser);
        return address;
    }
}
