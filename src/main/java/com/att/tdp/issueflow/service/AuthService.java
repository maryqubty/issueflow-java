package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.request.LoginRequest;
import com.att.tdp.issueflow.dto.response.AuthResponse;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.entity.RevokedToken;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.repository.RevokedTokenRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import com.att.tdp.issueflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        String token = jwtService.generate(req.getUsername());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .build();
    }

    public void logout(String bearerHeader) {
        if (bearerHeader != null && bearerHeader.startsWith("Bearer ")) {
            String token = bearerHeader.substring(7);
            revokedTokenRepository.save(
                    RevokedToken.builder().token(token).revokedAt(LocalDateTime.now()).build());
        }
    }

    public UserResponse me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
