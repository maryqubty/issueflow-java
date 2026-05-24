package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.response.AttachmentResponse;
import com.att.tdp.issueflow.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/tickets/{ticketId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(attachmentService.upload(ticketId, file));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long ticketId,
            @PathVariable Long attachmentId) {
        attachmentService.delete(ticketId, attachmentId);
        return ResponseEntity.ok().build();
    }
}
