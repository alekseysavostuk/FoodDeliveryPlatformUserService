package v1.foodDeliveryPlatform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.service.UserCleanupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCleanupServiceImpl implements UserCleanupService {

    private final UserRepository userRepository;

    public void cleanupUnconfirmedUsers() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusMinutes(5);

        List<User> unconfirmedUsers = userRepository
                .findByEmailConfirmedFalseAndCreatedAtBefore(oneHourAgo);

        if (!unconfirmedUsers.isEmpty()) {
            userRepository.deleteAll(unconfirmedUsers);
            log.info("Auto-cleaned {} unconfirmed users older than 1 hour",
                    unconfirmedUsers.size());
        }
    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void autoCleanup() {
        log.debug("Starting automatic cleanup of unconfirmed users");
        cleanupUnconfirmedUsers();
    }
}
