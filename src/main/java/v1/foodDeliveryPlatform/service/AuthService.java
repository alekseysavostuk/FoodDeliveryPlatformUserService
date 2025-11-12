package v1.foodDeliveryPlatform.service;

import v1.foodDeliveryPlatform.dto.auth.JwtResponse;
import v1.foodDeliveryPlatform.model.User;

public interface AuthService {
    JwtResponse loginWithEmailAndPassword(String username, String password);

    User createUser(User user);

    JwtResponse refresh(String refreshToken);
}