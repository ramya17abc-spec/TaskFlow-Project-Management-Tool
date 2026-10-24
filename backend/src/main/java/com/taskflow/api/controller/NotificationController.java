package com.taskflow.api.controller;

import com.taskflow.api.dto.ApiModels.NotificationResponse;
import com.taskflow.api.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications){this.notifications=notifications;}
    @GetMapping public List<NotificationResponse> list(){return notifications.list();}
    @PatchMapping("/{id}/read") public NotificationResponse markRead(@PathVariable Long id){return notifications.markRead(id);}
}
