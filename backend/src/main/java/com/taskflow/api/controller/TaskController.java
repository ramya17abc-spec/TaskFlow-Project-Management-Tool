package com.taskflow.api.controller;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.model.*;
import com.taskflow.api.repository.TaskRepository;
import com.taskflow.api.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {
    private final TaskService tasks; private final CommentService comments;
    public TaskController(TaskService tasks,CommentService comments){this.tasks=tasks;this.comments=comments;}
    @GetMapping("/tasks") public List<TaskResponse> all(){return tasks.all();}
    @GetMapping("/projects/{projectId}/tasks") public List<TaskResponse> list(@PathVariable Long projectId,@RequestParam(required=false)String q,@RequestParam(required=false)TaskStatus status,@RequestParam(required=false)TaskPriority priority){return tasks.list(projectId,q,status,priority);}
    @PostMapping("/projects/{projectId}/tasks") @ResponseStatus(HttpStatus.CREATED) public TaskResponse create(@PathVariable Long projectId,@Valid @RequestBody TaskRequest request){return tasks.create(projectId,request);}
    @GetMapping("/tasks/mine") public List<TaskResponse> mine(){return tasks.mine();}
    @PutMapping("/tasks/{id}") public TaskResponse update(@PathVariable Long id,@Valid @RequestBody TaskRequest request){return tasks.update(id,request);}
    @PatchMapping("/tasks/{id}/status") public TaskResponse status(@PathVariable Long id,@Valid @RequestBody StatusRequest request){return tasks.changeStatus(id,request.status());}
    @DeleteMapping("/tasks/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){tasks.delete(id);}
    @GetMapping("/tasks/{id}/comments") public List<CommentResponse> comments(@PathVariable Long id){return comments.list(id);}
    @PostMapping("/tasks/{id}/comments") @ResponseStatus(HttpStatus.CREATED) public CommentResponse comment(@PathVariable Long id,@Valid @RequestBody CommentRequest request){return comments.add(id,request);}
}
