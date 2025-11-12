package v1.foodDeliveryPlatform.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import v1.foodDeliveryPlatform.dto.validation.OnCreate;
import v1.foodDeliveryPlatform.dto.validation.OnUpdate;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data transfer object")
public class UserDto {

    @NotNull(message = "Id must be not null",
            groups = OnUpdate.class)
    @Schema(
            description = "Unique user identifier (required only for updates)",
            example = "123e4567-e89b-12d3-a456-426614174000",
            format = "uuid"
    )
    private UUID id;

    @NotBlank(message = "Email must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Email should be in valid format",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "Email must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "User's email address",
            example = "user@example.com",
            maxLength = 255,
            format = "email"
    )
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password must be not blank",
            groups = OnCreate.class)
    @Schema(
            description = "User password (write only, not returned in responses)",
            example = "1234",
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String password;

    @NotBlank(message = "Name must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "Email must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "User's full name",
            example = "John Doe",
            maxLength = 255
    )
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(
            description = "List of user addresses (read only)",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<AddressDto> addressDtoList;
}
