package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.request.LoginRequest;
import com.att.tdp.issueflow.dto.response.AuthResponse;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.me(userDetails.getUsername()));
    }
}
