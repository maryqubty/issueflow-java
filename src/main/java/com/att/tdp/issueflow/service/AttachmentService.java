package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.response.AttachmentResponse;
import com.att.tdp.issueflow.entity.Attachment;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.exception.ValidationException;
import com.att.tdp.issueflow.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "application/pdf", "text/plain"
    );

    private final AttachmentRepository attachmentRepository;
    private final TicketService ticketService;

    public AttachmentResponse upload(Long ticketId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_SIZE) {
            throw new ValidationException("File exceeds the 10 MB size limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException("File type not allowed: " + contentType +
                    ". Allowed: image/png, image/jpeg, application/pdf, text/plain");
        }

        Ticket ticket = ticketService.findOrThrow(ticketId);
        Attachment attachment = Attachment.builder()
                .ticket(ticket)
                .filename(file.getOriginalFilename())
                .contentType(contentType)
                .data(file.getBytes())
                .build();
        Attachment saved = attachmentRepository.save(attachment);
        return toResponse(saved);
    }

    public void delete(Long ticketId, Long attachmentId) {
        ticketService.findOrThrow(ticketId);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));
        attachmentRepository.delete(attachment);
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .ticketId(attachment.getTicket().getId())
                .filename(attachment.getFilename())
                .contentType(attachment.getContentType())
                .build();
    }
}
