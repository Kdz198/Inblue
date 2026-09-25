package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.model.Notification;
import fpt.org.inblue.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceActiveFlowTest {
    @Mock
    NotificationRepository repository;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(repository);
    }

    @Test
    void userNotificationsQueryUsesUserId() {
        when(repository.findAllByUser_Id(7)).thenReturn(List.of(new Notification()));
        service.getNotifications(7);
        verify(repository).findAllByUser_Id(7);
    }

    @Test
    void existingNotificationCanBeMarkedRead() {
        Notification notification = new Notification();
        when(repository.existsById(3)).thenReturn(true);
        when(repository.findById(3)).thenReturn(Optional.of(notification));
        assertTrue(service.checkRead(3));
        assertTrue(notification.getIsRead());
        verify(repository).save(notification);
    }

    @Test
    void missingNotificationDoesNotWrite() {
        when(repository.existsById(404)).thenReturn(false);
        assertFalse(service.checkRead(404));
    }
}
