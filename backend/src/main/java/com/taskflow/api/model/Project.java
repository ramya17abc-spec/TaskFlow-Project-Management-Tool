package com.taskflow.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="projects")
public class Project {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=120) private String name;
    @Column(length=800) private String description;
    @Column(length=24) private String color = "violet";
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id") private User owner;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    protected Project() {}
    public Project(String name,String description,String color,User owner){this.name=name.trim();this.description=description;this.color=color;this.owner=owner;}
    public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public String getColor(){return color;} public User getOwner(){return owner;} public Instant getCreatedAt(){return createdAt;}
    public void update(String name,String description,String color){this.name=name.trim();this.description=description;this.color=color;}
}
