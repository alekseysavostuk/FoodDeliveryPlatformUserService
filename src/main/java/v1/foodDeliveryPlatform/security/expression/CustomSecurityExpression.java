package v1.foodDeliveryPlatform.security.expression;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;
import v1.foodDeliveryPlatform.model.Address;
import v1.foodDeliveryPlatform.model.Role;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.security.jwt.JwtUser;
import v1.foodDeliveryPlatform.service.AddressService;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("expression")
@RequiredArgsConstructor
@Slf4j
public class CustomSecurityExpression {

    private final RoleRepository roleRepository;
    private final AddressService addressService;

    @Transactional
    public boolean isAccessUser(UUID id) {
        log.debug("Checking user access for ID: {}", id);

        JwtUser user = getPrincipal();
        UUID userId = user.getId();

        boolean hasAccess = userId.equals(id) || hasAnyRole(roleRepository.findByName("ROLE_ADMIN"));

        if (hasAccess) {
            log.debug("Access GRANTED for user {} to user resource {}", user.getUsername(), id);
        } else {
            log.warn("Access DENIED for user {} to user resource {}", user.getUsername(), id);
        }

        return hasAccess;
    }

    public boolean isAccessAddress(UUID addressId) {
        log.debug("Checking address access for address ID: {}", addressId);

        JwtUser user = getPrincipal();

        try {
            Address address = addressService.getById(addressId);
            boolean hasAccess = user.getId().equals(address.getUser().getId());

            if (hasAccess) {
                log.debug("Access GRANTED for user {} to address {}", user.getUsername(), addressId);
            } else {
                log.warn("Access DENIED for user {} to address {} (owner: {})",
                        user.getUsername(), addressId, address.getUser().getId());
            }

            return hasAccess;
        } catch (ResourceNotFoundException e) {
            log.warn("Address not found during access check: {}", addressId);
            return false;
        } catch (Exception e) {
            log.error("Error checking address access for address ID: {}", addressId, e);
            return false;
        }
    }

    private boolean hasAnyRole(Set<Role> roles) {
        log.trace("Checking if user has any of roles: {}",
                roles.stream().map(Role::getName).collect(Collectors.toSet()));

        if (roles.isEmpty()) {
            log.trace("No roles provided for check");
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            log.warn("No authentication or authorities found in security context");
            return false;
        }

        boolean hasRole = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .anyMatch(authority -> authentication.getAuthorities().contains(authority));

        log.trace("Role check result: {}", hasRole);
        return hasRole;
    }

    private JwtUser getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.error("No authentication found in security context");
            throw new IllegalStateException("No authentication found");
        }

        if (!(authentication.getPrincipal() instanceof JwtUser)) {
            log.error("Unexpected principal type: {}", authentication.getPrincipal().getClass().getName());
            throw new IllegalStateException("Principal is not JwtUser");
        }

        JwtUser user = (JwtUser) authentication.getPrincipal();
        log.trace("Retrieved principal: {} ({})", user.getUsername(), user.getId());

        return user;
    }
}
