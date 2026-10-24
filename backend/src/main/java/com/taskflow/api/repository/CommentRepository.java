package com.taskflow.api.repository;

import com.taskflow.api.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<TaskComment,Long> { List<TaskComment> findByTask_IdOrderByCreatedAtAsc(Long taskId); long countByTask_Id(Long taskId); }
