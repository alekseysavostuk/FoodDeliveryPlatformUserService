package v1.foodDeliveryPlatform.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.exception.ModelExistsException;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.RoleRepository;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.security.JwtTokenUtil;
import v1.foodDeliveryPlatform.security.JwtUserDetailsService;
import v1.foodDeliveryPlatform.service.AuthService;

import java.util.Date;

@Service
@Slf4j
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public String loginWithEmailAndPassword(String email, String password){
        authenticate(email, password);
        final UserDetails userDetails = jwtUserDetailsService
                .loadUserByUsername(email);
        return jwtTokenUtil.generateToken(userDetails);

    }

    private void authenticate(String email, String password){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new DisabledException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", e);
        }
    }

    @Override
    public void saveUser(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new ModelExistsException("Username already taken");
        }
        user.setRoles(roleRepository.findByName("ROLE_USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreated(new Date());
        userRepository.save(user);
    }
}
