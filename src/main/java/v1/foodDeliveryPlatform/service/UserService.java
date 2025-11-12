package v1.foodDeliveryPlatform.service;

import v1.foodDeliveryPlatform.model.User;

import java.util.UUID;

public interface UserService {
    User getById(UUID id);

    User updateUser(User user);

    void delete(UUID id);

    User updateRole(UUID id);

    User getByEmail(String email);
}
