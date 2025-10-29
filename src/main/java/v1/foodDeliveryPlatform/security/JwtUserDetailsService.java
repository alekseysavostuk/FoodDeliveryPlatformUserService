package v1.foodDeliveryPlatform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.exception.NotFoundException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.UserRepository;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException(
                        String.format("The city with name %S isn't found", username)));

        List<String> roles = new ArrayList<>();
        user.getRoles().forEach(role -> roles.add(role.getName()));

        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();

        for (String role: roles){
            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role));
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), simpleGrantedAuthorities);
    }
}