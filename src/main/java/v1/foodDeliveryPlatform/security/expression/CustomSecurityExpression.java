package v1.foodDeliveryPlatform.security.expression;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import v1.foodDeliveryPlatform.model.Role;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.security.jwt.JwtUser;
import v1.foodDeliveryPlatform.service.AddressService;

import java.util.Set;
import java.util.UUID;

@Component("expression")
@RequiredArgsConstructor
public class CustomSecurityExpression {


    private final RoleRepository roleRepository;
    private final AddressService addressService;

    @Transactional
    public boolean isAccessUser(UUID id) {
        JwtUser user = getPrincipal();
        UUID userId = user.getId();

        return userId.equals(id) || hasAnyRole(roleRepository.findByName("ROLE_ADMIN"));
    }

    public boolean isAccessAddress(UUID addressId) {
        JwtUser user = getPrincipal();
        return user.getId().equals(addressService.getById(addressId).getUser().getId());
    }

    private boolean hasAnyRole(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .anyMatch(authority -> authentication.getAuthorities().contains(authority));
    }

    private JwtUser getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        return (JwtUser) authentication.getPrincipal();
    }
}
