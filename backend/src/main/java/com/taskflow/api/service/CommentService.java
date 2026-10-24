package com.taskflow.api.service;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.model.*;
import com.taskflow.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository comments; private final TaskService tasks; private final ProjectService projects; private final CurrentUser current; private final NotificationRepository notifications;
    public CommentService(CommentRepository comments,TaskService tasks,ProjectService projects,CurrentUser current,NotificationRepository notifications){this.comments=comments;this.tasks=tasks;this.projects=projects;this.current=current;this.notifications=notifications;}
    @Transactional(readOnly=true) public List<CommentResponse> list(Long taskId){TaskItem task=tasks.requireTask(taskId);projects.requireAccess(task.getProject().getId());return comments.findByTask_IdOrderByCreatedAtAsc(taskId).stream().map(this::toResponse).toList();}
    @Transactional public CommentResponse add(Long taskId,CommentRequest request){TaskItem task=tasks.requireTask(taskId);projects.requireAccess(task.getProject().getId());User author=current.get();TaskComment comment=comments.save(new TaskComment(task,author,request.body()));User assignee=task.getAssignee();if(assignee!=null&&!assignee.getId().equals(author.getId()))notifications.save(new AppNotification(assignee,task.getProject(),task,author.getDisplayName()+" commented on "+task.getTitle()));return toResponse(comment);}
    private CommentResponse toResponse(TaskComment c){return new CommentResponse(c.getId(),c.getBody(),c.getAuthor().getId(),c.getAuthor().getDisplayName(),c.getCreatedAt());}
}
