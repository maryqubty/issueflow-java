package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.request.CreateUserRequest;
import com.att.tdp.issueflow.dto.request.UpdateUserRequest;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.exception.ConflictException;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username already exists: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already exists: " + req.getEmail());
        }
        String hash = passwordEncoder.encode(req.getPassword() != null ? req.getPassword() : "default");
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .fullName(req.getFullName())
                .role(req.getRole())
                .passwordHash(hash)
                .build();
        return toResponse(userRepository.save(user));
    }

    public void update(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getRole() != null) user.setRole(req.getRole());
        userRepository.save(user);
    }

    public void delete(Long id) {
        findOrThrow(id);
        userRepository.deleteById(id);
    }

    public User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
