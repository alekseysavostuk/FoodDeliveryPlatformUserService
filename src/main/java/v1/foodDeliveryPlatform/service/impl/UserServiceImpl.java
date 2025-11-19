package v1.foodDeliveryPlatform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.service.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#id")
    public User getById(UUID id) {
        log.debug("Fetching user from database by ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found with ID: {}", id);
            return new ResourceNotFoundException("User not found");
        });
        log.debug("Successfully fetched user: {} ({})", user.getEmail(), user.getId());
        return user;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#user.id"),
            @CacheEvict(value = "users_by_email", key = "#result.email")
    })
    public User updateUser(User user) {

        log.info("Updating user with ID: {}", user.getId());
        User currentUser = getById(user.getId());

        boolean emailChanged = !currentUser.getEmail().equals(user.getEmail());
        log.debug("User update - email changed: {}, name: {}", emailChanged, user.getName());

        currentUser.setEmail(user.getEmail());
        currentUser.setName(user.getName());
        currentUser.setEmailConfirmed(user.isEmailConfirmed());
        currentUser.setUpdated(LocalDateTime.now());

        log.info("=== UPDATING USER ===");
        log.info("Email: {}", user.getEmail());
        log.info("Email confirmed: {}", user.isEmailConfirmed());
        log.info("Password hash: {}", user.getPassword());
        log.info("Roles: {}", user.getRoles());
        log.info("=====================");

        User updatedUser = userRepository.save(currentUser);
        log.info("User successfully updated: {} ({})", updatedUser.getEmail(), updatedUser.getId());
        return updatedUser;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users_by_email", allEntries = true)
    })
    public void delete(UUID id) {
        log.info("Deleting user with ID: {}", id);
        try {
            userRepository.deleteById(id);
            log.info("User successfully deleted: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete user with ID: {}", id, e);
            throw e;
        }
    }

    public User updateRole(UUID id) {
        log.info("Updating role for user with ID: {}", id);
        User currentUser = getById(id);

        String previousRole = currentUser.getRoles().toString();
        if (currentUser.getRoles().equals(roleRepository.findByName("ROLE_USER"))) {
            currentUser.setRoles(roleRepository.findByName("ROLE_MANAGER"));
            log.debug("User role changed from ROLE_USER to ROLE_MANAGER");
        } else {
            currentUser.setRoles(roleRepository.findByName("ROLE_USER"));
            log.debug("User role changed from {} to ROLE_USER", previousRole);
        }

        User updatedUser = userRepository.save(currentUser);
        log.info("Role successfully updated for user: {} ({})", updatedUser.getEmail(), updatedUser.getId());
        return updatedUser;
    }

    @Override
    @Transactional
    @Cacheable(value = "users_by_email", key = "#email")
    public User getByEmail(String email) {
        log.debug("Fetching user from database by email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User not found with email: {}", email);
            return new ResourceNotFoundException("User not found");
        });
        log.debug("Successfully fetched user by email: {} ({})", user.getEmail(), user.getId());
        return user;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users_by_email", allEntries = true)
    })
    public User changePassword(UUID id, String newRawPassword) {
        log.info("Changing password for user ID: {}", id);

        User user = getById(id);
        user.setPassword(passwordEncoder.encode(newRawPassword));
        user.setUpdated(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user: {}", updatedUser.getEmail());
        return updatedUser;
    }
}
