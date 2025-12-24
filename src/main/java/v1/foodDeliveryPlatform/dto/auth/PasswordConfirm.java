package v1.foodDeliveryPlatform.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for confirmation password")
public class PasswordConfirm {

    @NotBlank(message = "Password must be not blank")
    @Schema(
            description = "User's password",
            example = "1234"
    )
    private String password;
}
