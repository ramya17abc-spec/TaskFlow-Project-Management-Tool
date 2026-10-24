package com.taskflow.api.service;

import com.taskflow.api.model.User;
import com.taskflow.api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class CurrentUser {
    private final UserRepository users;
    public CurrentUser(UserRepository users){this.users=users;}
    public User get(){String email=SecurityContextHolder.getContext().getAuthentication().getName();return users.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Account no longer exists"));}
}
