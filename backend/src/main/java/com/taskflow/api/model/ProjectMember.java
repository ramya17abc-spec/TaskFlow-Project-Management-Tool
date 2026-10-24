package com.taskflow.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="project_members",uniqueConstraints=@UniqueConstraint(columnNames={"project_id","user_id"}))
public class ProjectMember {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="project_id") private Project project;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id") private User user;
    @Column(nullable=false,length=20) private String role="MEMBER";
    @Column(nullable=false,updatable=false) private Instant joinedAt=Instant.now();
    protected ProjectMember() {}
    public ProjectMember(Project project,User user,String role){this.project=project;this.user=user;this.role=role;}
    public Long getId(){return id;} public Project getProject(){return project;} public User getUser(){return user;} public String getRole(){return role;} public Instant getJoinedAt(){return joinedAt;}
}
