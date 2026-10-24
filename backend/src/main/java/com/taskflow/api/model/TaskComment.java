package com.taskflow.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="task_comments",indexes=@Index(name="idx_comment_task_created",columnList="task_id,created_at"))
public class TaskComment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="task_id") private TaskItem task;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="author_id") private User author;
    @Column(nullable=false,length=2000) private String body;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    protected TaskComment() {}
    public TaskComment(TaskItem task,User author,String body){this.task=task;this.author=author;this.body=body.trim();}
    public Long getId(){return id;} public TaskItem getTask(){return task;} public User getAuthor(){return author;} public String getBody(){return body;} public Instant getCreatedAt(){return createdAt;}
}
