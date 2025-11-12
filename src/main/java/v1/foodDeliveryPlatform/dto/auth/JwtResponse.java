package v1.foodDeliveryPlatform.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT authentication response with tokens and user data")
public class JwtResponse {

    @Schema(
            description = "Unique user identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            format = "uuid"
    )
    private UUID id;

    @Schema(
            description = "User's email address",
            example = "user@example.com",
            format = "email"
    )
    private String email;

    @Schema(
            description = "JWT access token for API authorization",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String accessToken;

    @Schema(
            description = "JWT refresh token for obtaining new access tokens",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String refreshToken;
}
