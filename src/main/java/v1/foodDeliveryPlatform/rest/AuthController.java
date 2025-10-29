package v1.foodDeliveryPlatform.rest;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import v1.foodDeliveryPlatform.dto.auth.AccessTokenDto;
import v1.foodDeliveryPlatform.dto.auth.AuthDto;
import v1.foodDeliveryPlatform.dto.model.UserDto;
import v1.foodDeliveryPlatform.facade.AuthFacade;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/auth")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AccessTokenDto> loginWithNameAndPassword(@RequestBody @Valid AuthDto authDto) {
        return new ResponseEntity<>(authFacade.getToken(authDto), HttpStatus.OK);
    }

    @PostMapping("/registering")
    @PreAuthorize("permitAll()")
    public ResponseEntity saveUser(@RequestBody @Valid UserDto userDto) {
        authFacade.saveUser(userDto);
        return new ResponseEntity(HttpStatus.OK);
    }
}