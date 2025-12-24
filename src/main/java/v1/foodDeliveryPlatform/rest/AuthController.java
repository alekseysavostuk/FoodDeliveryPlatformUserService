package v1.foodDeliveryPlatform.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import v1.foodDeliveryPlatform.dto.auth.JwtRequest;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.dto.auth.RefreshTokenRequest;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.dto.validation.OnCreate;
import v1.foodDeliveryPlatform.facade.AuthFacade;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true"
)
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
    @Operation(summary = "Register User")
    public ResponseEntity<String> register(
            @Validated(OnCreate.class)
            @RequestBody UserDto userDto) throws MessagingException {
        authFacade.createUser(userDto);
        return new ResponseEntity<>("Registration email sent", HttpStatus.OK);
    }

    @GetMapping("/confirm-email")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Confirm email")
    public ResponseEntity<Map<String, String>> confirmEmail(
            @RequestParam String email,
            @RequestParam String code) {
        try {
            authFacade.confirmEmail(email, code);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email confirmed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Confirmation failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens")
    public JwtResponse refresh(
            @RequestBody RefreshTokenRequest refreshToken) {
        return authFacade.refreshToken(refreshToken);
    }
}