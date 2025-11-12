package v1.foodDeliveryPlatform.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import v1.foodDeliveryPlatform.dto.auth.JwtRequest;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.dto.auth.RefreshTokenRequest;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.dto.validation.OnCreate;
import v1.foodDeliveryPlatform.facade.AuthFacade;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Tag(
        name = "Auth Controller",
        description = "Auth API"
)
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Login User")
    public ResponseEntity<JwtResponse> loginWithEmailAndPassword(
            @Validated
            @RequestBody JwtRequest jwtRequest) {
        return new ResponseEntity<>(authFacade.getToken(jwtRequest), HttpStatus.OK);
    }

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    @Operation(summary = "User registration")
    public ResponseEntity<UserDto> createUser(
            @Validated(OnCreate.class)
            @RequestBody UserDto userDto) {
        return new ResponseEntity<>(authFacade.createUser(userDto), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens")
    public JwtResponse refresh(
            @RequestBody RefreshTokenRequest refreshToken) {
        return authFacade.refreshToken(refreshToken);
    }
}