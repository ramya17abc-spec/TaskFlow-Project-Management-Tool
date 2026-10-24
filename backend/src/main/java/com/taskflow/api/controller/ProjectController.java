package com.taskflow.api.controller;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projects;
    public ProjectController(ProjectService projects){this.projects=projects;}
    @GetMapping public List<ProjectResponse> list(){return projects.list();}
    @GetMapping("/{id}") public ProjectResponse get(@PathVariable Long id){return projects.get(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ProjectResponse create(@Valid @RequestBody ProjectRequest request){return projects.create(request);}
    @PutMapping("/{id}") public ProjectResponse update(@PathVariable Long id,@Valid @RequestBody ProjectRequest request){return projects.update(id,request);}
    @GetMapping("/{id}/members") public List<MemberResponse> members(@PathVariable Long id){return projects.listMembers(id);}
    @PostMapping("/{id}/members") @ResponseStatus(HttpStatus.CREATED) public MemberResponse addMember(@PathVariable Long id,@Valid @RequestBody MemberRequest request){return projects.addMember(id,request);}
}
