package com.taskflow.api.security;

import com.taskflow.api.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public AppUserDetailsService(UserRepository users){this.users=users;}
    @Override public UserDetails loadUserByUsername(String email){var user=users.findByEmail(email.toLowerCase()).orElseThrow(()->new UsernameNotFoundException("Account not found"));return User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles("USER").build();}
}
