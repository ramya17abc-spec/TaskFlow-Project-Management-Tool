package com.taskflow.api.dto;

import com.taskflow.api.model.TaskPriority;
import com.taskflow.api.model.TaskStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ApiModels {
    private ApiModels() {}
    public record SignupRequest(@NotBlank @Size(max=90) String displayName,@NotBlank @Email @Size(max=160) String email,@NotBlank @Size(min=10,max=72) String password) {}
    public record LoginRequest(@NotBlank @Email String email,@NotBlank String password) {}
    public record AuthResponse(String token,UserResponse user) {}
    public record UserResponse(Long id,String displayName,String email,String jobTitle,Instant createdAt) {}
    public record ProfileRequest(@NotBlank @Size(max=90) String displayName,@Size(max=120) String jobTitle) {}
    public record ProjectRequest(@NotBlank @Size(max=120) String name,@Size(max=800) String description,@Pattern(regexp="[a-zA-Z0-9_-]{1,24}") String color) {}
    public record ProjectResponse(Long id,String name,String description,String color,Long ownerId,String ownerName,long taskCount,long completedCount,Instant createdAt) {}
    public record TaskRequest(@NotBlank @Size(max=180) String title,@Size(max=2000) String description,TaskStatus status,TaskPriority priority,LocalDate dueDate,@Size(max=40) String tag,Long assigneeId) {}
    public record StatusRequest(@NotNull TaskStatus status) {}
    public record TaskResponse(Long id,String title,String description,TaskStatus status,TaskPriority priority,LocalDate dueDate,String tag,Long projectId,String projectName,Long assigneeId,String assigneeName,String assigneeEmail,Long createdById,Instant createdAt,Instant updatedAt,long commentCount) {}
    public record CommentRequest(@NotBlank @Size(max=2000) String body) {}
    public record CommentResponse(Long id,String body,Long authorId,String authorName,Instant createdAt) {}
    public record MemberRequest(@NotBlank @Email String email,@Pattern(regexp="MEMBER|ADMIN") String role) {}
    public record MemberResponse(Long id,String displayName,String email,String role,Instant joinedAt) {}
    public record NotificationResponse(Long id,String message,Long projectId,String projectName,Long taskId,boolean read,Instant createdAt) {}
    public record MessageResponse(String message) {}
    public record ErrorResponse(String error,String message,Instant timestamp) {}
    public record TeamResponse(List<MemberResponse> members) {}
}
