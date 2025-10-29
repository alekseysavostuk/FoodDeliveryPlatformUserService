package v1.foodDeliveryPlatform.service;

import v1.foodDeliveryPlatform.model.User;

public interface AuthService {
    String loginWithEmailAndPassword(String username, String password);
    void saveUser (User user);
}