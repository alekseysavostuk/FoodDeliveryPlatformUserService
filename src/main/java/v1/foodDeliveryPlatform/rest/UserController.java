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
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.dto.validation.OnCreate;
import v1.foodDeliveryPlatform.dto.validation.OnUpdate;
import v1.foodDeliveryPlatform.facade.AddressFacade;
import v1.foodDeliveryPlatform.facade.UserFacade;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(
        name = "User Controller",
        description = "User API"
)
public class UserController {

    private final UserFacade userFacade;
    private final AddressFacade addressFacade;

    @PutMapping
    @Operation(summary = "Update user")
    @PreAuthorize("@expression.isAccessUser(#userDto.id)")
    public ResponseEntity<UserDto> updateUser(
            @Validated(OnUpdate.class)
            @RequestBody UserDto userDto) {
        return new ResponseEntity<>(userFacade.updateUser(userDto), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @PreAuthorize("@expression.isAccessUser(#id)")
    public ResponseEntity<UserDto> getById(
            @PathVariable final UUID id) {
        return new ResponseEntity<>(userFacade.getById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    @PreAuthorize("@expression.isAccessUser(#id)")
    public ResponseEntity<Void> deleteById(
            @PathVariable final UUID id) {
        userFacade.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user role (available to admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateRole(
            @Validated(OnUpdate.class)
            @PathVariable final UUID id) {
        return new ResponseEntity<>(userFacade.updateRole(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/addresses")
    @Operation(summary = "Add address to user")
    @PreAuthorize("@expression.isAccessUser(#id)")
    public ResponseEntity<AddressDto> createAddress(
            @Validated(OnCreate.class)
            @PathVariable final UUID id,
            @RequestBody AddressDto addressDto) {
        return new ResponseEntity<>(addressFacade.createAddress(addressDto, id), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/addresses")
    @Operation(summary = "Get addresses by user id")
    @PreAuthorize("@expression.isAccessUser(#id)")
    public ResponseEntity<List<AddressDto>> getAddressesByUserId(
            @PathVariable final UUID id) {
        return new ResponseEntity<>(addressFacade.getAllByUserId(id), HttpStatus.OK);
    }

}
