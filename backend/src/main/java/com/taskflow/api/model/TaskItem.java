package com.taskflow.api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="tasks",indexes={@Index(name="idx_task_project_status",columnList="project_id,status"),@Index(name="idx_task_assignee",columnList="assignee_id")})
public class TaskItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=180) private String title;
    @Column(length=2000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private TaskStatus status=TaskStatus.BACKLOG;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=12) private TaskPriority priority=TaskPriority.MEDIUM;
    private LocalDate dueDate;
    @Column(length=40) private String tag;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="project_id") private Project project;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assignee_id") private User assignee;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="created_by_id") private User createdBy;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    @Column(nullable=false) private Instant updatedAt=Instant.now();
    protected TaskItem() {}
    public TaskItem(String title,String description,TaskStatus status,TaskPriority priority,LocalDate dueDate,String tag,Project project,User assignee,User createdBy){this.title=title.trim();this.description=description;this.status=status;this.priority=priority;this.dueDate=dueDate;this.tag=tag;this.project=project;this.assignee=assignee;this.createdBy=createdBy;}
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public TaskStatus getStatus(){return status;} public TaskPriority getPriority(){return priority;} public LocalDate getDueDate(){return dueDate;} public String getTag(){return tag;} public Project getProject(){return project;} public User getAssignee(){return assignee;} public User getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void update(String title,String description,TaskStatus status,TaskPriority priority,LocalDate dueDate,String tag,User assignee){this.title=title.trim();this.description=description;this.status=status;this.priority=priority;this.dueDate=dueDate;this.tag=tag;this.assignee=assignee;this.updatedAt=Instant.now();}
    public void changeStatus(TaskStatus status){this.status=status;this.updatedAt=Instant.now();}
}
