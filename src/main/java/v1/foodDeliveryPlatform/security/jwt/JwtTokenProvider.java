package v1.foodDeliveryPlatform.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.exception.TokenException;
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
public class JwtTokenProvider {

    private final JwtProps jwtProps;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProps.getSecret().getBytes());
    }

    public String createAccessToken(String email, Set<Role> roles, UUID id) {

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", id);
        claims.put("roles", getRole(roles));
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProps.getAccess());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    private List<String> getRole(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }

    public String createRefreshToken(UUID id, String email) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", id);
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProps.getRefresh());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public JwtResponse refreshTokens(String refreshToken) {
        JwtResponse jwtResponse = new JwtResponse();
        if (!isValid(refreshToken)) {
            throw new TokenException("Invalid token");
        }
        UUID id = UUID.fromString(getId(refreshToken));
        User user = userService.getById(id);
        jwtResponse.setId(id);
        jwtResponse.setEmail(user.getEmail());
        jwtResponse.setAccessToken(
                createAccessToken(user.getEmail(), user.getRoles(), id));
        jwtResponse.setRefreshToken(
                createRefreshToken(id, user.getEmail()));
        return jwtResponse;
    }

    public boolean isValid(String token) {
        try {
            Jws<Claims> claims = Jwts
                    .parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            boolean isValid = !claims.getBody().getExpiration().before(new Date());
            System.out.println("Token validation result: " + isValid);

            return isValid;

        } catch (ExpiredJwtException e) {
            System.err.println("Token EXPIRED: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("Token MALFORMED: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.err.println("Token SIGNATURE INVALID: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Token ILLEGAL ARGUMENT: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Token validation UNEXPECTED ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getId(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", String.class);
    }

    private String getEmail(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Authentication getAuthentication(
            final String token
    ) {
        String email = getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());
    }

}
