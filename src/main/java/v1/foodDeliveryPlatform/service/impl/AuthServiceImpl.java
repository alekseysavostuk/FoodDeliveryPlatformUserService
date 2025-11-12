package v1.foodDeliveryPlatform.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.exception.ModelExistsException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.security.jwt.JwtTokenProvider;
import v1.foodDeliveryPlatform.service.AuthService;
import v1.foodDeliveryPlatform.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public JwtResponse loginWithEmailAndPassword(String email, String password) {
        authenticate(email, password);
        User user = userService.getByEmail(email);
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(user.getId());
        jwtResponse.setEmail(email);
        jwtResponse.setAccessToken(
                jwtTokenProvider.createAccessToken(email, user.getRoles(), user.getId()));
        jwtResponse.setRefreshToken(
                jwtTokenProvider.createRefreshToken(user.getId(), email));
        return jwtResponse;
    }

    private void authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new DisabledException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", e);
        }
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ModelExistsException("User already taken");
        }
        user.setRoles(roleRepository.findByName("ROLE_USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreated(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    @Override
    public JwtResponse refresh(@RequestBody String refreshToken) {
        return jwtTokenProvider.refreshTokens(refreshToken);
    }
}
