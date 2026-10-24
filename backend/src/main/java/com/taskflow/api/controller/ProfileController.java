package com.taskflow.api.controller;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.model.User;
import com.taskflow.api.service.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/users")
public class ProfileController {
    private final CurrentUser current;
    public ProfileController(CurrentUser current){this.current=current;}
    @GetMapping("/me") public UserResponse get(){return response(current.get());}
    @PatchMapping("/me") @Transactional public UserResponse update(@Valid @RequestBody ProfileRequest request){User user=current.get();user.setDisplayName(request.displayName());user.setJobTitle(request.jobTitle());return response(user);}
    private UserResponse response(User u){return new UserResponse(u.getId(),u.getDisplayName(),u.getEmail(),u.getJobTitle(),u.getCreatedAt());}
}
