package com.taskflow.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="notifications",indexes=@Index(name="idx_notification_user_read",columnList="recipient_id,is_read"))
public class AppNotification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="recipient_id") private User recipient;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id") private Project project;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="task_id") private TaskItem task;
    @Column(nullable=false,length=320) private String message;
    @Column(name="is_read",nullable=false) private boolean read=false;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    protected AppNotification() {}
    public AppNotification(User recipient,Project project,TaskItem task,String message){this.recipient=recipient;this.project=project;this.task=task;this.message=message;}
    public Long getId(){return id;} public User getRecipient(){return recipient;} public Project getProject(){return project;} public TaskItem getTask(){return task;} public String getMessage(){return message;} public boolean isRead(){return read;} public Instant getCreatedAt(){return createdAt;} public void markRead(){read=true;}
}
