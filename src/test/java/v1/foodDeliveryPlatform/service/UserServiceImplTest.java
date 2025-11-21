package v1.foodDeliveryPlatform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
import v1.foodDeliveryPlatform.model.Role;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.service.impl.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private final UUID userId = UUID.randomUUID();
    private final String userEmail = "test@example.com";

    @Test
    void getById_Success() {
        User user = createTestUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void getById_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getById(userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void getByEmail_Success() {
        User user = createTestUser();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        User result = userService.getByEmail(userEmail);

        assertNotNull(result);
        assertEquals(userEmail, result.getEmail());
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void getByEmail_NotFound() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getByEmail(userEmail));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void updateUser_Success() {
        User existingUser = createTestUser();
        User updateData = createTestUser();
        updateData.setEmail("new@example.com");
        updateData.setName("New Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updateUser(updateData);

        assertNotNull(result);
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals("New Name", existingUser.getName());
        assertNotNull(existingUser.getUpdated());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_NotFound() {
        User updateData = createTestUser();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(updateData));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        doNothing().when(userRepository).deleteById(userId);

        assertDoesNotThrow(() -> userService.delete(userId));

        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_Exception() {
        doThrow(new RuntimeException("DB error")).when(userRepository).deleteById(userId);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.delete(userId));

        assertEquals("DB error", exception.getMessage());
        verify(userRepository).deleteById(userId);
    }

    @Test
    void updateRole_FromUserToManager() {
        User user = createTestUser();
        Role userRole = createRole(1L, "ROLE_USER");
        Role managerRole = createRole(2L, "ROLE_MANAGER");
        user.setRoles(Set.of(userRole));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Set.of(userRole));
        when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(Set.of(managerRole));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateRole(userId);

        assertNotNull(result);
        assertEquals(Set.of(managerRole), user.getRoles());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void updateRole_FromManagerToUser() {
        User user = createTestUser();
        Role managerRole = createRole(2L, "ROLE_MANAGER");
        Role userRole = createRole(1L, "ROLE_USER");
        user.setRoles(Set.of(managerRole));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Set.of(userRole));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateRole(userId);

        assertNotNull(result);
        assertEquals(Set.of(userRole), user.getRoles());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_Success() {
        User user = createTestUser();
        String newPassword = "newPassword123";
        String encodedPassword = "encodedPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changePassword(userId, newPassword);

        assertNotNull(result);
        assertEquals(encodedPassword, user.getPassword());
        assertNotNull(user.getUpdated());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_UserNotFound() {
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.changePassword(userId, newPassword));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    private User createTestUser() {
        User user = new User();
        user.setId(userId);
        user.setEmail(userEmail);
        user.setName("Test User");
        user.setPassword("password");
        user.setEmailConfirmed(true);
        user.setCreated(LocalDateTime.now());
        return user;
    }

    private Role createRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
