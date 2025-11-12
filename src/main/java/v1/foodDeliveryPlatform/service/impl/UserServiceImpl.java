package v1.foodDeliveryPlatform.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.exception.ModelExistsException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.service.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ModelExistsException("User not found"));
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User currentuser = getById(user.getId());
        currentuser.setEmail(user.getEmail());
        currentuser.setPassword(passwordEncoder.encode(user.getPassword()));
        currentuser.setName(user.getName());
        currentuser.setUpdated(LocalDateTime.now());
        userRepository.save(currentuser);
        return currentuser;
    }

    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateRole(UUID id) {
        User currentUser = getById(id);
        if (currentUser.getRoles().equals(roleRepository.findByName("ROLE_USER"))) {
            currentUser.setRoles(roleRepository.findByName("ROLE_MANAGER"));
        } else {
            currentUser.setRoles(roleRepository.findByName("ROLE_USER"));
        }
        userRepository.save(currentUser);
        return currentUser;
    }

    @Override
    @Transactional
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new ModelExistsException("User not found"));
    }
}
