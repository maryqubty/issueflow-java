package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.request.CreateUserRequest;
import com.att.tdp.issueflow.dto.request.UpdateUserRequest;
import com.att.tdp.issueflow.dto.response.MentionsPageResponse;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.service.CommentService;
import com.att.tdp.issueflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @PostMapping("/update/{userId}")
    public ResponseEntity<Void> update(@PathVariable Long userId, @RequestBody UpdateUserRequest req) {
        userService.update(userId, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/mentions")
    public ResponseEntity<MentionsPageResponse> getMentions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(commentService.getMentions(userId, page, pageSize));
    }
}
