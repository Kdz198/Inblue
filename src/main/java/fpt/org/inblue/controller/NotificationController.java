package fpt.org.inblue.controller;

import fpt.org.inblue.model.Notification;
import fpt.org.inblue.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<List<Notification>> getAllNotifications(@PathVariable int id) {
        List<Notification> notifications = notificationService.getNotifications(id);
        return ResponseEntity.ok(notifications);
    }
    @Operation(summary = "json mẫu: {\n" +
            "  \"id\": 0,\n" +
            "  \"user\": {\n" +
            "    \"id\":1\n" +
            "  },\n" +
            "  \"title\": \"string\",\n" +
            "  \"message\": \"string\",\n" +
            "  \"isRead\": true,\n" +
            "  \"createAt\": \"2026-01-17T09:35:10.823Z\"\n" +
            "}")
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
