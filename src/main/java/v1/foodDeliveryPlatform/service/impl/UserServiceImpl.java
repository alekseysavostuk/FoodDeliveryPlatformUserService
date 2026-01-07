package v1.foodDeliveryPlatform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#id")
    @CrossOrigin(origins = "http://localhost:5173",
            methods = {RequestMethod.GET, RequestMethod.POST,
                    RequestMethod.PUT, RequestMethod.DELETE,
                    RequestMethod.OPTIONS, RequestMethod.PATCH},
            allowedHeaders = "*",
            allowCredentials = "true")
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

        currentUser.setName(user.getName());
        currentUser.setPhoneNumber(user.getPhoneNumber());
        currentUser.setUpdated(LocalDateTime.now());

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
        log.info("Blocking user with ID: {}", id);
        try {
            User user = getById(id);
            user.setEmailConfirmed(false);
            userRepository.save(user);
            log.info("User successfully blocked: {}", id);
        } catch (Exception e) {
            log.error("Failed to block user with ID: {}", id, e);
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
        String confirmationCode = generateConfirmationCode();

        cachePendingPassword(user.getEmail(), confirmationCode, newRawPassword);

        user.setConfirmationCode(confirmationCode);
        user.setUpdated(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user: {}", updatedUser.getEmail());
        return updatedUser;
    }

    @Override
    @Transactional
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users", users.size());
        return users;
    }

    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void cachePendingPassword(String email, String confirmationCode, String newPassword) {
        String cacheKey = buildCacheKey(email, confirmationCode);

        try {
            Cache cache = cacheManager.getCache("pending_passwords");
            if (cache != null) {
                cache.put(cacheKey, newPassword);
                log.debug("Password cached via CacheManager: {}", cacheKey);
            }

            log.info("New password cached for email: {} with TTL: {} minutes",
                    email, 10);

        } catch (Exception e) {
            log.error("Failed to cache password for email: {}", email, e);
            throw new RuntimeException("Failed to cache password", e);
        }
    }

    private String buildCacheKey(String email, String confirmationCode) {
        return email + ":" + confirmationCode;
    }

}
