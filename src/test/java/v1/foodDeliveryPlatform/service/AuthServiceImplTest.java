package v1.foodDeliveryPlatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.exception.EmailNotConfirmedException;
import v1.foodDeliveryPlatform.model.Role;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.security.jwt.JwtTokenProvider;
import v1.foodDeliveryPlatform.service.impl.AuthServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String encodedPassword = "encodedPassword123";
    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, "ROLE_USER");
        testUser = User.builder()
                .id(uuid)
                .email(testEmail)
                .password(encodedPassword)
                .emailConfirmed(true)
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and confirmed email")
    void loginWithEmailAndPassword_Success() {

        JwtResponse expectedResponse = new JwtResponse();
        expectedResponse.setId(uuid);
        expectedResponse.setEmail(testEmail);
        expectedResponse.setAccessToken("access-token");
        expectedResponse.setRefreshToken("refresh-token");

        when(userService.getByEmail(testEmail)).thenReturn(testUser);
        when(jwtTokenProvider.createAccessToken(eq(testEmail), anySet(), eq(uuid)))
                .thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(uuid, testEmail))
                .thenReturn("refresh-token");


        JwtResponse result = authService.loginWithEmailAndPassword(testEmail, testPassword);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getEmail(), result.getEmail());
        assertEquals(expectedResponse.getAccessToken(), result.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), result.getRefreshToken());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword));
        verify(userService).getByEmail(testEmail);
        verify(jwtTokenProvider).createAccessToken(eq(testEmail), anySet(), eq(uuid));
        verify(jwtTokenProvider).createRefreshToken(uuid, testEmail);
    }

    @Test
    @DisplayName("Should throw EmailNotConfirmedException when email is not confirmed")
    void loginWithEmailAndPassword_EmailNotConfirmed() {

        testUser.setEmailConfirmed(false);
        when(userService.getByEmail(testEmail)).thenReturn(testUser);


        EmailNotConfirmedException exception = assertThrows(EmailNotConfirmedException.class,
                () -> authService.loginWithEmailAndPassword(testEmail, testPassword));

        assertEquals("Email not confirmed. Please check your email for confirmation link.",
                exception.getMessage());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword));
        verify(userService).getByEmail(testEmail);
        verify(jwtTokenProvider, never()).createAccessToken(anyString(), anySet(), any());
        verify(jwtTokenProvider, never()).createRefreshToken(any(), anyString());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when authentication fails")
    void loginWithEmailAndPassword_InvalidCredentials() {

        doThrow(new BadCredentialsException("INVALID_CREDENTIALS"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));


        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.loginWithEmailAndPassword(testEmail, testPassword));

        assertEquals("INVALID_CREDENTIALS", exception.getMessage());

        verify(userService, never()).getByEmail(anyString());
        verify(jwtTokenProvider, never()).createAccessToken(anyString(), anySet(), any());
    }

    @Test
    @DisplayName("Should throw DisabledException when user is disabled")
    void loginWithEmailAndPassword_UserDisabled() {

        doThrow(new DisabledException("USER_DISABLED"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));


        DisabledException exception = assertThrows(DisabledException.class,
                () -> authService.loginWithEmailAndPassword(testEmail, testPassword));

        assertEquals("USER_DISABLED", exception.getMessage());

        verify(userService, never()).getByEmail(anyString());
        verify(jwtTokenProvider, never()).createAccessToken(anyString(), anySet(), any());
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_Success() throws Exception {

        User newUser = User.builder()
                .email("newuser@example.com")
                .password("plainPassword")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("newuser@example.com")
                .password(encodedPassword)
                .emailConfirmed(false)
                .confirmationCode("abc12345")
                .created(LocalDateTime.now())
                .roles(Set.of(userRole))
                .build();

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Set.of(userRole));
        when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        User result = authService.createUser(newUser);


        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getEmail(), result.getEmail());
        assertFalse(result.isEmailConfirmed());
        assertNotNull(result.getConfirmationCode());
        assertEquals(8, result.getConfirmationCode().length());
        assertNotNull(result.getCreated());

        verify(userRepository).findByEmail(newUser.getEmail());
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void createUser_EmailAlreadyExists() {

        User existingUser = User.builder()
                .email("existing@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmail(existingUser.getEmail()))
                .thenReturn(Optional.of(existingUser));


        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.createUser(existingUser));

        assertEquals("User already taken", exception.getMessage());

        verify(userRepository).findByEmail(existingUser.getEmail());
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should refresh tokens successfully")
    void refresh_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        JwtResponse expectedResponse = new JwtResponse();
        expectedResponse.setAccessToken("new-access-token");
        expectedResponse.setRefreshToken("new-refresh-token");

        when(jwtTokenProvider.refreshTokens(refreshToken)).thenReturn(expectedResponse);


        JwtResponse result = authService.refresh(refreshToken);


        assertNotNull(result);
        assertEquals(expectedResponse.getAccessToken(), result.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), result.getRefreshToken());

        verify(jwtTokenProvider).refreshTokens(refreshToken);
    }

    @Test
    @DisplayName("Should propagate exception when token refresh fails")
    void refresh_Failure() {

        String invalidRefreshToken = "invalid-token";
        when(jwtTokenProvider.refreshTokens(invalidRefreshToken))
                .thenThrow(new RuntimeException("Invalid token"));


        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.refresh(invalidRefreshToken));

        assertEquals("Invalid token", exception.getMessage());

        verify(jwtTokenProvider).refreshTokens(invalidRefreshToken);
    }

    @Test
    @DisplayName("Should authenticate successfully with valid credentials")
    void authenticate_Success() {

        authService.authenticate(testEmail, testPassword);


        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword));
    }

    @Test
    @DisplayName("Should throw DisabledException when authenticating disabled user")
    void authenticate_UserDisabled() {

        doThrow(new DisabledException("USER_DISABLED"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));


        DisabledException exception = assertThrows(DisabledException.class,
                () -> authService.authenticate(testEmail, testPassword));

        assertEquals("USER_DISABLED", exception.getMessage());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException with invalid credentials")
    void authenticate_InvalidCredentials() {

        doThrow(new BadCredentialsException("INVALID_CREDENTIALS"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));


        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(testEmail, testPassword));

        assertEquals("INVALID_CREDENTIALS", exception.getMessage());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword));
    }

    @Test
    @DisplayName("Should generate confirmation code of correct length")
    void generateConfirmationCode_ShouldReturn8Characters() throws Exception {

        var method = AuthServiceImpl.class.getDeclaredMethod("generateConfirmationCode");
        method.setAccessible(true);

        String code = (String) method.invoke(authService);

        assertNotNull(code);
        assertEquals(8, code.length());


        for (int i = 0; i < 10; i++) {
            String newCode = (String) method.invoke(authService);
            assertEquals(8, newCode.length());
        }
    }
}
