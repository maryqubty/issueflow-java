package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.response.AttachmentResponse;
import com.att.tdp.issueflow.entity.Attachment;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketService ticketService;

    public AttachmentResponse upload(Long ticketId, MultipartFile file) throws IOException {
        Ticket ticket = ticketService.findOrThrow(ticketId);
        Attachment attachment = Attachment.builder()
                .ticket(ticket)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
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
