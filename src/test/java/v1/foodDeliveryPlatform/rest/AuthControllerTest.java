package v1.foodDeliveryPlatform.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import v1.foodDeliveryPlatform.dto.auth.JwtRequest;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.dto.auth.RefreshTokenRequest;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.AuthFacade;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthFacade authFacade;

    private JwtRequest jwtRequest;
    private String validUserJson;
    private JwtResponse jwtResponse;
    private RefreshTokenRequest refreshTokenRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        jwtRequest = new JwtRequest("test@example.com", "password123");

        validUserJson = """
                {
                    "id": "%s",
                    "email": "test@example.com",
                    "password": "ValidPassword123!",
                    "name": "Test User"
                }
                """.formatted(userId);

        jwtResponse = new JwtResponse();
        jwtResponse.setId(userId);
        jwtResponse.setEmail("test@example.com");
        jwtResponse.setAccessToken("access-token");
        jwtResponse.setRefreshToken("refresh-token");

        refreshTokenRequest = new RefreshTokenRequest("refresh-token");
    }

    @Test
    @WithMockUser
    void loginWithEmailAndPassword_Success() throws Exception {

        when(authFacade.getToken(any(JwtRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authFacade).getToken(any(JwtRequest.class));
    }

    @Test
    @WithMockUser
    void loginWithEmailAndPassword_InvalidInput() throws Exception {

        JwtRequest invalidRequest = new JwtRequest("invalid-email", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authFacade, never()).getToken(any(JwtRequest.class));
    }

    @Test
    @WithMockUser
    void register_Success() throws Exception {

        doNothing().when(authFacade).createUser(any(UserDto.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration email sent"));

        verify(authFacade).createUser(any(UserDto.class));
    }

    @Test
    @WithMockUser
    void register_ValidationError() throws Exception {

        String invalidUserJson = """
                {
                    "email": "invalid-email",
                    "name": "Test User"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest());

        verify(authFacade, never()).createUser(any(UserDto.class));
    }

    @Test
    @WithMockUser
    void confirmEmail_Success() throws Exception {

        doNothing().when(authFacade).confirmEmail("test@example.com", "123456");

        mockMvc.perform(get("/api/v1/auth/confirm-email")
                        .param("email", "test@example.com")
                        .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Email confirmed successfully"));

        verify(authFacade).confirmEmail("test@example.com", "123456");
    }

    @Test
    @WithMockUser
    void confirmEmail_Failure() throws Exception {

        doThrow(new RuntimeException("Invalid code")).when(authFacade).confirmEmail("test@example.com", "wrong-code");

        mockMvc.perform(get("/api/v1/auth/confirm-email")
                        .param("email", "test@example.com")
                        .param("code", "wrong-code"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Confirmation failed: Invalid code"));

        verify(authFacade).confirmEmail("test@example.com", "wrong-code");
    }

    @Test
    @WithMockUser
    void refresh_Success() throws Exception {

        when(authFacade.refreshToken(any(RefreshTokenRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authFacade).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @WithMockUser
    void refresh_InvalidToken() throws Exception {

        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void loginWithEmailAndPassword_FacadeThrowsException() throws Exception {

        when(authFacade.getToken(any(JwtRequest.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().is5xxServerError());

        verify(authFacade).getToken(any(JwtRequest.class));
    }

    @Test
    @WithMockUser
    void refresh_FacadeThrowsException() throws Exception {

        when(authFacade.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new RuntimeException("Token refresh failed"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().is5xxServerError());

        verify(authFacade).refreshToken(any(RefreshTokenRequest.class));
    }
}

