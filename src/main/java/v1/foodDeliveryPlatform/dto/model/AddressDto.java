package v1.foodDeliveryPlatform.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import v1.foodDeliveryPlatform.dto.validation.OnCreate;
import v1.foodDeliveryPlatform.dto.validation.OnUpdate;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address data transfer object")
public class AddressDto {

    @NotNull(message = "Id must be not null",
            groups = OnUpdate.class)
    @Schema(
            description = "Unique address identifier (required only for updates)",
            example = "123e4567-e89b-12d3-a456-426614174000",
            format = "uuid"
    )
    private UUID id;

    @NotBlank(message = "Street must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "Street must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "Street address",
            example = "123 Main Street",
            maxLength = 255
    )
    private String street;

    @NotBlank(message = "City must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "City must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "City name",
            example = "New York",
            maxLength = 255
    )
    private String city;

    @NotBlank(message = "Zip must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "Zip must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "Postal/ZIP code",
            example = "10001",
            maxLength = 255
    )
    private String zip;

    @NotBlank(message = "State must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "State must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "State or province",
            example = "NY",
            maxLength = 255
    )
    private String state;

    @NotBlank(message = "Country must be not blank",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "Country must be smaller 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "Country name",
            example = "United States",
            maxLength = 255
    )
    private String country;

}
