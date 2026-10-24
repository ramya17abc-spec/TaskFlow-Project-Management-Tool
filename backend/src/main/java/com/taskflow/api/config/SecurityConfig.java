package com.taskflow.api.config;

import com.taskflow.api.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
    @Bean DaoAuthenticationProvider authenticationProvider(AppUserDetailsService users,PasswordEncoder encoder){var provider=new DaoAuthenticationProvider(users);provider.setPasswordEncoder(encoder);return provider;}
    @Bean AuthenticationManager authenticationManager(DaoAuthenticationProvider provider){return new ProviderManager(provider);}
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwtFilter,DaoAuthenticationProvider provider)throws Exception{
        return http.csrf(csrf->csrf.disable()).cors(cors->{}).sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authenticationProvider(provider).authorizeHttpRequests(auth->auth.requestMatchers("/api/auth/signup","/api/auth/login","/api/health").permitAll().anyRequest().authenticated()).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}")String origin){var config=new CorsConfiguration();config.setAllowedOrigins(List.of(origin));config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));config.setExposedHeaders(List.of("Location"));config.setAllowCredentials(false);var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/api/**",config);return source;}
}
