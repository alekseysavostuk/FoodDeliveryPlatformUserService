package v1.foodDeliveryPlatform.service.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.model.enums.MailType;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.security.jwt.JwtTokenProvider;
import v1.foodDeliveryPlatform.service.AuthService;
import v1.foodDeliveryPlatform.service.EmailService;
import v1.foodDeliveryPlatform.service.UserService;

import java.time.LocalDateTime;
import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public JwtResponse loginWithEmailAndPassword(String email, String password) {
        log.info("Attempting login for email: {}", email);

        try {
            authenticate(email, password);
            User user = userService.getByEmail(email);

            log.debug("User authenticated successfully: {} ({})", email, user.getId());

            JwtResponse jwtResponse = new JwtResponse();
            jwtResponse.setId(user.getId());
            jwtResponse.setEmail(email);
            jwtResponse.setAccessToken(
                    jwtTokenProvider.createAccessToken(email, user.getRoles(), user.getId()));
            jwtResponse.setRefreshToken(
                    jwtTokenProvider.createRefreshToken(user.getId(), email));

            log.info("Login successful for user: {} ({})", email, user.getId());
            return jwtResponse;

        } catch (DisabledException e) {
            log.warn("Login failed - user disabled: {}", email);
            throw e;
        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for: {}", email, e);
            throw e;
        }
    }

    private void authenticate(String email, String password) {
        log.debug("Authenticating user: {}", email);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            log.debug("Authentication successful for: {}", email);
        } catch (DisabledException e) {
            log.error("Authentication failed - user disabled: {}", email);
            throw new DisabledException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            log.error("Authentication failed - bad credentials for: {}", email);
            throw new BadCredentialsException("INVALID_CREDENTIALS", e);
        }
    }

    @Override
    @Transactional
    public User createUser(User user) throws MessagingException {
        log.info("Creating new user with email: {}", user.getEmail());

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("User creation failed - email already taken: {}", user.getEmail());
            throw new IllegalStateException("User already taken");
        }

        user.setRoles(roleRepository.findByName("ROLE_USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreated(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {} ({})", savedUser.getEmail(), savedUser.getId());

        try {
            Properties params = new Properties();
            emailService.sendEmail(savedUser, MailType.REGISTRATION, params);
            log.debug("Registration email sent to: {}", savedUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send registration email to: {}", savedUser.getEmail(), e);
            // Не бросаем исключение дальше - пользователь уже создан
        }

        return savedUser;
    }

    @Override
    public JwtResponse refresh(@RequestBody String refreshToken) {
        log.debug("Refreshing tokens with refresh token");

        try {
            JwtResponse jwtResponse = jwtTokenProvider.refreshTokens(refreshToken);
            log.debug("Tokens refreshed successfully for user: {}", jwtResponse.getEmail());
            return jwtResponse;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }
}
