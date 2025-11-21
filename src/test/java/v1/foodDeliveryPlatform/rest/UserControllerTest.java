package v1.foodDeliveryPlatform.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import v1.foodDeliveryPlatform.config.ControllerTestSecurityConfig;
import v1.foodDeliveryPlatform.dto.auth.ChangePasswordRequest;
import v1.foodDeliveryPlatform.dto.model.AddressDto;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.AddressFacade;
import v1.foodDeliveryPlatform.facade.UserFacade;
import v1.foodDeliveryPlatform.security.expression.CustomSecurityExpression;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ControllerTestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserFacade userFacade;

    @MockitoBean
    private AddressFacade addressFacade;

    @MockitoBean
    private CustomSecurityExpression expression;

    private final UUID userId = UUID.randomUUID();
    private final String userJson = """
        {
            "id": "%s",
            "email": "test@example.com",
            "name": "Test User"
        }
        """.formatted(userId);

    private final String addressJson = """
        {
            "city": "Warsaw",
            "street": "Test Street",
            "zipCode": "00-001"
        }
        """;

    private final String changePasswordJson = """
        {
            "newPassword": "newPassword123"
        }
        """;

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        when(userFacade.updateUser(any(UserDto.class))).thenReturn(userDto);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        verify(userFacade).updateUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_Success() throws Exception {
        UserDto userDto = new UserDto();
        when(userFacade.updateRole(userId)).thenReturn(userDto);

        mockMvc.perform(patch("/api/v1/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userFacade).updateRole(userId);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void updateRole_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userFacade, never()).updateRole(any(UUID.class));
    }

    @Test
    void getById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isUnauthorized());

        verify(userFacade, never()).getById(any(UUID.class));
    }

    @Test
    @WithMockUser
    void changePassword_Success() throws Exception {
        UserDto userDto = new UserDto();
        when(userFacade.changePassword(any(UUID.class), any(ChangePasswordRequest.class))).thenReturn(userDto);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(patch("/api/v1/users/{id}/change-password", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson))
                .andExpect(status().isOk());

        verify(userFacade).changePassword(any(UUID.class), any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser
    void getById_Success() throws Exception {
        UserDto userDto = new UserDto();
        when(userFacade.getById(userId)).thenReturn(userDto);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userFacade).getById(userId);
    }

    @Test
    @WithMockUser
    void deleteById_Success() throws Exception {
        doNothing().when(userFacade).delete(userId);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userFacade).delete(userId);
    }

    @Test
    @WithMockUser
    void createAddress_Success() throws Exception {
        AddressDto addressDto = new AddressDto();
        when(addressFacade.createAddress(any(AddressDto.class), eq(userId))).thenReturn(addressDto);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/users/{id}/addresses", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson))
                .andExpect(status().isCreated());

        verify(addressFacade).createAddress(any(AddressDto.class), eq(userId));
    }

    @Test
    @WithMockUser
    void getAddressesByUserId_Success() throws Exception {
        List<AddressDto> addresses = List.of(new AddressDto());
        when(addressFacade.getAllByUserId(userId)).thenReturn(addresses);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/{id}/addresses", userId))
                .andExpect(status().isOk());

        verify(addressFacade).getAllByUserId(userId);
    }

    @Test
    @WithMockUser
    void updateUser_ValidationError() throws Exception {
        String invalidUserJson = """
        {
            "id": "%s",
            "email": "invalid-email",
            "name": ""
        }
        """.formatted(userId);
        when(expression.isAccessUser(any(UUID.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest());

        verify(userFacade, never()).updateUser(any(UserDto.class));
    }
}
