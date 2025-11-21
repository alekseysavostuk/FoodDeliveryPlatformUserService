package v1.foodDeliveryPlatform.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import v1.foodDeliveryPlatform.security.expression.CustomSecurityExpression;

import static org.mockito.Mockito.mock;
import static org.springframework.security.config.Customizer.withDefaults;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class ControllerTestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/{id}/change-password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/{id}/addresses").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}/addresses").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean(name = "expression")
    @Primary
    public CustomSecurityExpression customSecurityExpression() {
        return mock(CustomSecurityExpression.class);
    }
}
