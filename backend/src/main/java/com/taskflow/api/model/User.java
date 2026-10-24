package com.taskflow.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 160) private String email;
    @Column(nullable = false, length = 90) private String displayName;
    @Column(nullable = false) private String passwordHash;
    @Column(length = 120) private String jobTitle;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    protected User() {}
    public User(String email, String displayName, String passwordHash) { this.email=email.toLowerCase().trim(); this.displayName=displayName.trim(); this.passwordHash=passwordHash; }
    public Long getId(){return id;} public String getEmail(){return email;} public String getDisplayName(){return displayName;} public String getPasswordHash(){return passwordHash;} public String getJobTitle(){return jobTitle;} public Instant getCreatedAt(){return createdAt;}
    public void setDisplayName(String value){displayName=value.trim();} public void setJobTitle(String value){jobTitle=value;}
}
