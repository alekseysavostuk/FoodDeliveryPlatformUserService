package v1.foodDeliveryPlatform.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import v1.foodDeliveryPlatform.dto.validation.OnCreate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for restore account")
public class RestoreRequest {

    @NotBlank(message = "Email must be not blank",
            groups = OnCreate.class)
    @Email(message = "Email should be in valid format",
            groups = OnCreate.class)
    @Length(max = 255, message = "Email must be smaller 255 characters",
            groups = OnCreate.class)
    @Schema(
            description = "User's email address",
            example = "user@example.com",
            maxLength = 255,
            format = "email"
    )
    private String email;
}
