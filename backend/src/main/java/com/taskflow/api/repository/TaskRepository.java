package com.taskflow.api.repository;

import com.taskflow.api.model.TaskItem;
import com.taskflow.api.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskItem,Long> { List<TaskItem> findByProject_IdOrderByCreatedAtDesc(Long projectId); List<TaskItem> findByAssignee_IdOrderByDueDateAsc(Long userId); long countByProject_IdAndStatus(Long projectId,TaskStatus status); }
