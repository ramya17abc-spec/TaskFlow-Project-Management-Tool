package com.taskflow.api.service;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.model.*;
import com.taskflow.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository tasks; private final ProjectService projects; private final UserRepository users; private final ProjectMemberRepository members; private final CommentRepository comments; private final NotificationRepository notifications; private final CurrentUser current;
    public TaskService(TaskRepository tasks,ProjectService projects,UserRepository users,ProjectMemberRepository members,CommentRepository comments,NotificationRepository notifications,CurrentUser current){this.tasks=tasks;this.projects=projects;this.users=users;this.members=members;this.comments=comments;this.notifications=notifications;this.current=current;}
    @Transactional(readOnly=true) public List<TaskResponse> list(Long projectId,String q,TaskStatus status,TaskPriority priority){projects.requireAccess(projectId);String needle=q==null?"":q.toLowerCase();return tasks.findByProject_IdOrderByCreatedAtDesc(projectId).stream().filter(t->status==null||t.getStatus()==status).filter(t->priority==null||t.getPriority()==priority).filter(t->needle.isBlank()||t.getTitle().toLowerCase().contains(needle)||(t.getTag()!=null&&t.getTag().toLowerCase().contains(needle))).map(this::toResponse).toList();}
    @Transactional(readOnly=true) public List<TaskResponse> all(){return projects.list().stream().flatMap(project->list(project.id(),null,null,null).stream()).toList();}
    @Transactional(readOnly=true) public List<TaskResponse> mine(){User user=current.get();return tasks.findByAssignee_IdOrderByDueDateAsc(user.getId()).stream().map(this::toResponse).toList();}
    @Transactional public TaskResponse create(Long projectId,TaskRequest request){Project project=projects.requireAccess(projectId);User actor=current.get();User assignee=assignee(projectId,request.assigneeId());TaskItem task=tasks.save(new TaskItem(request.title(),request.description(),request.status()==null?TaskStatus.BACKLOG:request.status(),request.priority()==null?TaskPriority.MEDIUM:request.priority(),request.dueDate(),request.tag(),project,assignee,actor));notifyAssignee(project,task,actor,"assigned you a task: "+task.getTitle());return toResponse(task);}
    @Transactional public TaskResponse update(Long id,TaskRequest request){TaskItem task=requireTask(id);projects.requireAccess(task.getProject().getId());User actor=current.get();User assignee=assignee(task.getProject().getId(),request.assigneeId());task.update(request.title(),request.description(),request.status()==null?task.getStatus():request.status(),request.priority()==null?task.getPriority():request.priority(),request.dueDate(),request.tag(),assignee);if(assignee!=null&&!assignee.getId().equals(actor.getId()))notifyAssignee(task.getProject(),task,actor,"assigned you a task: "+task.getTitle());return toResponse(task);}
    @Transactional public TaskResponse changeStatus(Long id,TaskStatus status){TaskItem task=requireTask(id);projects.requireAccess(task.getProject().getId());task.changeStatus(status);return toResponse(task);}
    @Transactional public void delete(Long id){TaskItem task=requireTask(id);projects.requireAccess(task.getProject().getId());User actor=current.get();boolean admin=task.getProject().getOwner().getId().equals(actor.getId())||members.findByProject_IdAndUser_Id(task.getProject().getId(),actor.getId()).map(m->m.getRole().equals("ADMIN")).orElse(false);if(!admin&&!task.getCreatedBy().getId().equals(actor.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the task creator or a project admin can delete this task");tasks.delete(task);}
    public TaskItem requireTask(Long id){return tasks.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found"));}
    private User assignee(Long projectId,Long userId){if(userId==null)return null;User user=users.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Assignee not found"));if(!members.existsByProject_IdAndUser_Id(projectId,userId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Assignee must belong to this project");return user;}
    private void notifyAssignee(Project project,TaskItem task,User actor,String action){if(task.getAssignee()!=null&&!task.getAssignee().getId().equals(actor.getId()))notifications.save(new AppNotification(task.getAssignee(),project,task,actor.getDisplayName()+" "+action));}
    private TaskResponse toResponse(TaskItem t){User a=t.getAssignee();return new TaskResponse(t.getId(),t.getTitle(),t.getDescription(),t.getStatus(),t.getPriority(),t.getDueDate(),t.getTag(),t.getProject().getId(),t.getProject().getName(),a==null?null:a.getId(),a==null?null:a.getDisplayName(),a==null?null:a.getEmail(),t.getCreatedBy().getId(),t.getCreatedAt(),t.getUpdatedAt(),comments.countByTask_Id(t.getId()));}
}
