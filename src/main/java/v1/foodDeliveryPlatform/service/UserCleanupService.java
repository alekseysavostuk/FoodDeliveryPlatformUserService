package v1.foodDeliveryPlatform.service;

public interface UserCleanupService {
    void cleanupUnconfirmedUsers();

    void autoCleanup();
}
