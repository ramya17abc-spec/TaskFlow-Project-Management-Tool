package com.taskflow.api.service;

import com.taskflow.api.dto.ApiModels.NotificationResponse;
import com.taskflow.api.model.AppNotification;
import com.taskflow.api.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications; private final CurrentUser current;
    public NotificationService(NotificationRepository notifications,CurrentUser current){this.notifications=notifications;this.current=current;}
    @Transactional(readOnly=true) public List<NotificationResponse> list(){return notifications.findTop50ByRecipient_IdOrderByCreatedAtDesc(current.get().getId()).stream().map(this::toResponse).toList();}
    @Transactional public NotificationResponse markRead(Long id){var notification=notifications.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));if(!notification.getRecipient().getId().equals(current.get().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Notification access required");notification.markRead();return toResponse(notification);}
    private NotificationResponse toResponse(AppNotification n){return new NotificationResponse(n.getId(),n.getMessage(),n.getProject()==null?null:n.getProject().getId(),n.getProject()==null?null:n.getProject().getName(),n.getTask()==null?null:n.getTask().getId(),n.isRead(),n.getCreatedAt());}
}
