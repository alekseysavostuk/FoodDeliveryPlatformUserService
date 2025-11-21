package v1.foodDeliveryPlatform.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import v1.foodDeliveryPlatform.config.ControllerTestSecurityConfig;
import v1.foodDeliveryPlatform.dto.model.AddressDto;
import v1.foodDeliveryPlatform.facade.AddressFacade;
import v1.foodDeliveryPlatform.security.expression.CustomSecurityExpression;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Import(ControllerTestSecurityConfig.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressFacade addressFacade;

    @Autowired
    @Qualifier("expression")
    private CustomSecurityExpression expression;

    private UUID addressId;
    private AddressDto addressDto;
    private String validAddressJson;

    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();

        addressDto = new AddressDto();
        addressDto.setId(addressId);
        addressDto.setCity("Warsaw");
        addressDto.setStreet("Test Street");
        addressDto.setZip("00-001");
        addressDto.setState("Mazovia");
        addressDto.setCountry("Poland");

        validAddressJson = """
                {
                    "id": "%s",
                    "city": "Warsaw",
                    "street": "Test Street",
                    "zip": "00-001",
                    "state": "Mazovia",
                    "country": "Poland"
                }
                """.formatted(addressId);

        reset(expression);
    }

    @Test
    @WithMockUser
    void updateAddress_Success() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);
        when(addressFacade.updateAddress(any(AddressDto.class))).thenReturn(addressDto);

        mockMvc.perform(put("/api/v1/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAddressJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andExpect(jsonPath("$.street").value("Test Street"))
                .andExpect(jsonPath("$.zip").value("00-001"));

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade).updateAddress(any(AddressDto.class));
    }

    @Test
    @WithMockUser
    void updateAddress_AccessDenied() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(false);

        mockMvc.perform(put("/api/v1/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAddressJson))
                .andExpect(status().isForbidden());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade, never()).updateAddress(any(AddressDto.class));
    }

    @Test
    @WithMockUser
    void getById_Success() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);
        when(addressFacade.getById(addressId)).thenReturn(addressDto);

        mockMvc.perform(get("/api/v1/addresses/{id}", addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andExpect(jsonPath("$.street").value("Test Street"))
                .andExpect(jsonPath("$.zip").value("00-001"));

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade).getById(addressId);
    }

    @Test
    @WithMockUser
    void getById_AccessDenied() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(false);

        mockMvc.perform(get("/api/v1/addresses/{id}", addressId))
                .andExpect(status().isForbidden());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade, never()).getById(any(UUID.class));
    }

    @Test
    @WithMockUser
    void deleteById_Success() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);
        doNothing().when(addressFacade).delete(addressId);

        mockMvc.perform(delete("/api/v1/addresses/{id}", addressId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade).delete(addressId);
    }

    @Test
    @WithMockUser
    void deleteById_AccessDenied() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(false);

        mockMvc.perform(delete("/api/v1/addresses/{id}", addressId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade, never()).delete(any(UUID.class));
    }


    @Test
    @WithMockUser
    void updateAddress_ValidationError() throws Exception {

        String invalidAddressJson = """
        {
            "id": "%s",
            "city": "",
            "street": "",
            "zip": "invalid-zip"
        }
        """.formatted(addressId);

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAddressJson))
                .andExpect(status().isBadRequest());

        verify(expression, atMostOnce()).isAccessAddress(eq(addressId));
        verify(addressFacade, never()).updateAddress(any(AddressDto.class));
    }

    @Test
    @WithMockUser
    void updateAddress_InternalServerError() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);
        when(addressFacade.updateAddress(any(AddressDto.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/v1/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAddressJson))
                .andExpect(status().isInternalServerError());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade).updateAddress(any(AddressDto.class));
    }

    @Test
    @WithMockUser
    void getById_InternalServerError() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);
        when(addressFacade.getById(addressId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/addresses/{id}", addressId))
                .andExpect(status().isInternalServerError());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade).getById(addressId);
    }

    @Test
    @WithMockUser
    void deleteById_InternalServerError() throws Exception {

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(addressFacade).delete(addressId);

        mockMvc.perform(delete("/api/v1/addresses/{id}", addressId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(expression).isAccessAddress(eq(addressId));
        verify(addressFacade).delete(addressId);
    }

    @Test
    @WithMockUser
    void updateAddress_MissingId() throws Exception {

        String jsonWithoutId = """
        {
            "city": "Warsaw",
            "street": "Test Street",
            "zip": "00-001",
            "state": "Mazovia",
            "country": "Poland"
        }
        """;

        when(expression.isAccessAddress(any(UUID.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutId))
                .andExpect(status().isBadRequest());

        verify(expression, never()).isAccessAddress(any(UUID.class));
        verify(addressFacade, never()).updateAddress(any(AddressDto.class));
    }
}