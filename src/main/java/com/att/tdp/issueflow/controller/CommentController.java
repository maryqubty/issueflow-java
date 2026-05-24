package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.request.CreateCommentRequest;
import com.att.tdp.issueflow.dto.request.UpdateCommentRequest;
import com.att.tdp.issueflow.dto.response.CommentResponse;
import com.att.tdp.issueflow.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getByTicket(ticketId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity.ok(commentService.create(ticketId, req));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> update(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest req) {
        commentService.update(ticketId, commentId, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long ticketId,
            @PathVariable Long commentId) {
        commentService.delete(ticketId, commentId);
        return ResponseEntity.ok().build();
    }
}
