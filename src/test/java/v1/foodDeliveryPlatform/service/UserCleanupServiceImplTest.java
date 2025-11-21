package v1.foodDeliveryPlatform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import v1.foodDeliveryPlatform.model.User;
import v1.foodDeliveryPlatform.repository.UserRepository;
import v1.foodDeliveryPlatform.service.impl.UserCleanupServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCleanupServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCleanupServiceImpl userCleanupService;

    @Test
    void cleanupUnconfirmedUsers_ShouldDeleteUnconfirmedUsers() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setEmailConfirmed(false);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setEmailConfirmed(false);

        List<User> unconfirmedUsers = Arrays.asList(user1, user2);

        when(userRepository.findByEmailConfirmedFalseAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(unconfirmedUsers);

        userCleanupService.cleanupUnconfirmedUsers();

        verify(userRepository).deleteAll(unconfirmedUsers);
    }

    @Test
    void cleanupUnconfirmedUsers_ShouldNotDeleteWhenNoUnconfirmedUsers() {
        when(userRepository.findByEmailConfirmedFalseAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        userCleanupService.cleanupUnconfirmedUsers();

        verify(userRepository, never()).deleteAll(any());
    }

    @Test
    void cleanupUnconfirmedUsers_ShouldLogDeletionCount() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setEmailConfirmed(false);

        List<User> unconfirmedUsers = List.of(user1);

        when(userRepository.findByEmailConfirmedFalseAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(unconfirmedUsers);

        userCleanupService.cleanupUnconfirmedUsers();

        verify(userRepository).deleteAll(unconfirmedUsers);
    }

    @Test
    void autoCleanup_ShouldCallCleanupUnconfirmedUsers() {
        when(userRepository.findByEmailConfirmedFalseAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        userCleanupService.autoCleanup();

        verify(userRepository).findByEmailConfirmedFalseAndCreatedAtBefore(any(LocalDateTime.class));
    }
}
