package com.taskflow.api.controller;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.model.User;
import com.taskflow.api.repository.UserRepository;
import com.taskflow.api.security.JwtService;
import com.taskflow.api.service.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager authenticationManager; private final JwtService jwt; private final CurrentUser current;
    public AuthController(UserRepository users,PasswordEncoder encoder,AuthenticationManager authenticationManager,JwtService jwt,CurrentUser current){this.users=users;this.encoder=encoder;this.authenticationManager=authenticationManager;this.jwt=jwt;this.current=current;}
    @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED) public AuthResponse signup(@Valid @RequestBody SignupRequest request){String email=request.email().toLowerCase().trim();if(users.existsByEmail(email))throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists");User user=users.save(new User(email,request.displayName(),encoder.encode(request.password())));return new AuthResponse(jwt.issue(user),response(user));}
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request){String email=request.email().toLowerCase().trim();try{authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));}catch(AuthenticationException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Email or password is incorrect");}User user=users.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Email or password is incorrect"));return new AuthResponse(jwt.issue(user),response(user));}
    @GetMapping("/me") public UserResponse me(){return response(current.get());}
    private UserResponse response(User u){return new UserResponse(u.getId(),u.getDisplayName(),u.getEmail(),u.getJobTitle(),u.getCreatedAt());}
}
