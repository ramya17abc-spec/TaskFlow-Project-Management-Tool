package com.taskflow.api.repository;

import com.taskflow.api.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification,Long> { List<AppNotification> findTop50ByRecipient_IdOrderByCreatedAtDesc(Long userId); }
