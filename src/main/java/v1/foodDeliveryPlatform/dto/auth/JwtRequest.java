package v1.foodDeliveryPlatform.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request with email and password")
public class JwtRequest {

    @NotBlank(message = "Email must be not blank")
    @Email(message = "Email should be in valid format")
    @Length(max = 255, message = "Email must be smaller 255 characters")
    @Schema(
            description = "User's email address",
            example = "user@example.com",
            format = "email"
    )
    private String email;

    @NotBlank(message = "Password must be not blank")
    @Schema(
            description = "User's password",
            example = "1234"
    )
    private String password;
}
