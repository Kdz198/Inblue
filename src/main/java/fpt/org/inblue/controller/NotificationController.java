package fpt.org.inblue.controller;

import fpt.org.inblue.model.Notification;
import fpt.org.inblue.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<List<Notification>> getAllNotifications(@PathVariable int id) {
        List<Notification> notifications = notificationService.getNotifications(id);
        return ResponseEntity.ok(notifications);
    }
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification createdNotification = notificationService.createNotification(notification);
        return ResponseEntity.ok(createdNotification);
    }
    @GetMapping("/check-read/{notificationId}")
    public ResponseEntity<Boolean> checkRead(@PathVariable int notificationId) {
        boolean result = notificationService.checkRead(notificationId);
        return ResponseEntity.ok(result);
    }
}
