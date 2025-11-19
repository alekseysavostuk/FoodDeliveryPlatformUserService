package v1.foodDeliveryPlatform.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.exception.AccessDeniedException;
import v1.foodDeliveryPlatform.model.Role;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.security.props.JwtProps;
import v1.foodDeliveryPlatform.service.UserService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProps jwtProps;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        log.info("Initializing JWT Token Provider with secret key");
        this.secretKey = Keys.hmacShaKeyFor(jwtProps.getSecret().getBytes());
        log.debug("JWT secret key initialized successfully");
    }

    public String createAccessToken(String email, Set<Role> roles, UUID id) {
        log.debug("Creating access token for user: {} ({})", email, id);

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", id);
        claims.put("roles", getRole(roles));
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProps.getAccess());

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        log.debug("Access token created successfully for user: {} (expires: {})",
                email, validity);
        return token;
    }

    private List<String> getRole(Set<Role> roles) {
        List<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toList());
        log.trace("Extracted roles: {}", roleNames);
        return roleNames;
    }

    public String createRefreshToken(UUID id, String email) {
        log.debug("Creating refresh token for user: {} ({})", email, id);

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", id);
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProps.getRefresh());

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        log.debug("Refresh token created successfully for user: {} (expires: {})",
                email, validity);
        return token;
    }

    public JwtResponse refreshTokens(String refreshToken) {
        log.info("Refreshing tokens with refresh token");

        if (!isValid(refreshToken)) {
            log.warn("Token refresh failed - invalid refresh token");
            throw new AccessDeniedException();
        }

        UUID id = UUID.fromString(getId(refreshToken));
        User user = userService.getById(id);

        log.debug("Token refresh successful for user: {} ({})", user.getEmail(), id);

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(id);
        jwtResponse.setEmail(user.getEmail());
        jwtResponse.setAccessToken(
                createAccessToken(user.getEmail(), user.getRoles(), id));
        jwtResponse.setRefreshToken(
                createRefreshToken(id, user.getEmail()));

        log.info("Tokens refreshed successfully for user: {}", user.getEmail());
        return jwtResponse;
    }

    public boolean isValid(String token) {
        log.trace("Validating JWT token");

        try {
            Jws<Claims> claims = Jwts
                    .parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            boolean isValid = !claims.getBody().getExpiration().before(new Date());

            if (isValid) {
                log.trace("Token validation SUCCESS - subject: {}", claims.getBody().getSubject());
            } else {
                log.warn("Token validation FAILED - token expired");
            }

            return isValid;

        } catch (ExpiredJwtException e) {
            log.warn("Token validation FAILED - token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token validation FAILED - malformed token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Token validation FAILED - invalid signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Token validation FAILED - illegal argument: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token validation FAILED - unexpected error: {}", e.getMessage(), e);
            return false;
        }
    }

    private String getId(String token) {
        log.trace("Extracting user ID from token");
        String id = Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", String.class);
        log.trace("Extracted user ID: {}", id);
        return id;
    }

    private String getEmail(String token) {
        log.trace("Extracting email from token");
        String email = Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        log.trace("Extracted email: {}", email);
        return email;
    }

    public Authentication getAuthentication(final String token) {
        log.debug("Getting authentication from token");

        String email = getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());

        log.debug("Authentication created for user: {}", email);
        return authentication;
    }

}
