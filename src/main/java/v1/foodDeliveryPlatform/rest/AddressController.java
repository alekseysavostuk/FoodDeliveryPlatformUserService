package v1.foodDeliveryPlatform.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import v1.foodDeliveryPlatform.dto.model.AddressDto;
import v1.foodDeliveryPlatform.dto.validation.OnUpdate;
import v1.foodDeliveryPlatform.facade.AddressFacade;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@AllArgsConstructor
@Tag(
        name = "Address Controller",
        description = "Address API"
)
public class AddressController {

    private final AddressFacade addressFacade;

    @PutMapping
    @Operation(summary = "Update address")
    @PreAuthorize("@expression.isAccessAddress(#addressDto.id)")
    public ResponseEntity<AddressDto> updateAddress(
            @Validated(OnUpdate.class)
            @RequestBody AddressDto addressDto) {
        return new ResponseEntity<>(addressFacade.updateAddress(addressDto), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by id")
    @PreAuthorize("@expression.isAccessAddress(#id)")
    public ResponseEntity<AddressDto> getById(
            @PathVariable final UUID id) {
        return new ResponseEntity<>(addressFacade.getById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address by id")
    @PreAuthorize("@expression.isAccessAddress(#id)")
    public ResponseEntity<Void> deleteById(
            @PathVariable final UUID id) {
        addressFacade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
