package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.request.CreateTicketRequest;
import com.att.tdp.issueflow.dto.request.UpdateTicketRequest;
import com.att.tdp.issueflow.dto.response.ImportResultResponse;
import com.att.tdp.issueflow.dto.response.TicketResponse;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.enums.*;
import com.att.tdp.issueflow.exception.ConflictException;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.exception.ValidationException;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<TicketResponse> getByProject(Long projectId) {
        return ticketRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .stream().map(this::toResponse).toList();
    }

    public TicketResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public TicketResponse create(CreateTicketRequest req) {
        Project project = projectService.findOrThrow(req.getProjectId());
        boolean autoAssigned = req.getAssigneeId() == null;
        User assignee = resolveAssignee(req.getAssigneeId(), project);

        Ticket ticket = Ticket.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus())
                .priority(req.getPriority())
                .type(req.getType())
                .project(project)
                .assignee(assignee)
                .dueDate(req.getDueDate())
                .createdAt(LocalDateTime.now())
                .build();
        Ticket saved = ticketRepository.save(ticket);
        auditLogService.record(AuditAction.CREATE, EntityType.TICKET, saved.getId(), null, Actor.USER);
        if (autoAssigned && assignee != null) {
            auditLogService.record(AuditAction.AUTO_ASSIGN, EntityType.TICKET, saved.getId(), null, Actor.SYSTEM);
        }
        return toResponse(saved);
    }

    public void update(Long id, UpdateTicketRequest req) {
        Ticket ticket = findOrThrow(id);

        if (ticket.getStatus() == TicketStatus.DONE) {
            throw new ConflictException("Cannot update a ticket that is already DONE");
        }

        if (req.getStatus() != null) {
            validateStatusTransition(ticket.getStatus(), req.getStatus(), id);
            ticket.setStatus(req.getStatus());
        }
        if (req.getTitle() != null) ticket.setTitle(req.getTitle());
        if (req.getDescription() != null) ticket.setDescription(req.getDescription());
        if (req.getPriority() != null) {
            ticket.setPriority(req.getPriority());
            ticket.setOverdue(false);
        }
        if (req.getDueDate() != null) ticket.setDueDate(req.getDueDate());
        if (req.getAssigneeId() != null) {
            ticket.setAssignee(userService.findOrThrow(req.getAssigneeId()));
        }
        ticketRepository.save(ticket);
        auditLogService.record(AuditAction.UPDATE, EntityType.TICKET, id, null, Actor.USER);
    }

    public void softDelete(Long id) {
        Ticket ticket = findOrThrow(id);
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        auditLogService.record(AuditAction.DELETE, EntityType.TICKET, id, null, Actor.USER);
    }

    public List<TicketResponse> getDeleted(Long projectId) {
        return ticketRepository.findByProjectIdAndDeletedAtIsNotNull(projectId)
                .stream().map(this::toResponse).toList();
    }

    public void restore(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + id));
        ticket.setDeletedAt(null);
        ticketRepository.save(ticket);
    }

    public byte[] exportCsv(Long projectId) throws IOException {
        List<Ticket> tickets = ticketRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                .withHeader("id", "title", "description", "status", "priority", "type", "assigneeId"))) {
            for (Ticket t : tickets) {
                printer.printRecord(
                        t.getId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getStatus(),
                        t.getPriority(),
                        t.getType(),
                        t.getAssignee() != null ? t.getAssignee().getId() : ""
                );
            }
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    public ImportResultResponse importCsv(MultipartFile file, Long projectId) throws IOException {
        Project project = projectService.findOrThrow(projectId);
        int created = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            for (CSVRecord record : parser) {
                try {
                    String title = record.get("title");
                    TicketStatus status = TicketStatus.valueOf(record.get("status"));
                    Priority priority = Priority.valueOf(record.get("priority"));
                    TicketType type = TicketType.valueOf(record.get("type"));
                    String assigneeIdStr = record.isMapped("assigneeId") ? record.get("assigneeId") : "";
                    User assignee = null;
                    if (!assigneeIdStr.isBlank()) {
                        assignee = userService.findOrThrow(Long.parseLong(assigneeIdStr));
                    }
                    Ticket ticket = Ticket.builder()
                            .title(title)
                            .description(record.isMapped("description") ? record.get("description") : null)
                            .status(status)
                            .priority(priority)
                            .type(type)
                            .project(project)
                            .assignee(assignee)
                            .createdAt(LocalDateTime.now())
                            .build();
                    ticketRepository.save(ticket);
                    created++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }
        return ImportResultResponse.builder()
                .created(created)
                .failed(failed)
                .errors(errors)
                .build();
    }

    public Ticket findOrThrow(Long id) {
        return ticketRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + id));
    }

    private void validateStatusTransition(TicketStatus current, TicketStatus next, Long ticketId) {
        if (next.ordinal() <= current.ordinal()) {
            throw new ValidationException(
                    "Invalid status transition: " + current + " → " + next + ". Status can only move forward.");
        }
        if (next == TicketStatus.DONE) {
            long blockers = ticketRepository.countUnresolvedBlockers(ticketId);
            if (blockers > 0) {
                throw new ValidationException(
                        "Cannot transition to DONE: ticket has " + blockers + " unresolved blocker(s)");
            }
        }
    }

    private User resolveAssignee(Long assigneeId, Project project) {
        if (assigneeId != null) {
            return userService.findOrThrow(assigneeId);
        }
        List<User> developers = userRepository.findByRole(Role.DEVELOPER);
        if (developers.isEmpty()) return null;
        Long projectId = project.getId();
        return developers.stream()
                .min(Comparator
                        .comparingLong((User u) ->
                                ticketRepository.countByAssigneeIdAndProjectIdAndStatusNotAndDeletedAtIsNull(
                                        u.getId(), projectId, TicketStatus.DONE))
                        .thenComparingLong(User::getId))
                .orElse(null);
    }

    public TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .type(ticket.getType())
                .projectId(ticket.getProject() != null ? ticket.getProject().getId() : null)
                .assigneeId(ticket.getAssignee() != null ? ticket.getAssignee().getId() : null)
                .dueDate(ticket.getDueDate())
                .overdue(ticket.isOverdue())
                .build();
    }
}
