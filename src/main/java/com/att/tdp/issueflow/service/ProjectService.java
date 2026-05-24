package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.request.CreateProjectRequest;
import com.att.tdp.issueflow.dto.request.UpdateProjectRequest;
import com.att.tdp.issueflow.dto.response.ProjectResponse;
import com.att.tdp.issueflow.dto.response.WorkloadResponse;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import com.att.tdp.issueflow.enums.TicketStatus;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final TicketRepository ticketRepository;

    public List<ProjectResponse> getAll() {
        return projectRepository.findAllByDeletedAtIsNull().stream().map(this::toResponse).toList();
    }

    public ProjectResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public ProjectResponse create(CreateProjectRequest req) {
        User owner = userService.findOrThrow(req.getOwnerId());
        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .build();
        Project saved = projectRepository.save(project);
        auditLogService.record(AuditAction.CREATE, EntityType.PROJECT, saved.getId(), req.getOwnerId(), Actor.USER);
        return toResponse(saved);
    }

    public void update(Long id, UpdateProjectRequest req) {
        Project project = findOrThrow(id);
        if (req.getName() != null) project.setName(req.getName());
        if (req.getDescription() != null) project.setDescription(req.getDescription());
        projectRepository.save(project);
        auditLogService.record(AuditAction.UPDATE, EntityType.PROJECT, id, null, Actor.USER);
    }

    public void softDelete(Long id) {
        Project project = findOrThrow(id);
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
        auditLogService.record(AuditAction.DELETE, EntityType.PROJECT, id, null, Actor.USER);
    }

    public List<ProjectResponse> getDeleted() {
        return projectRepository.findAllByDeletedAtIsNotNull().stream().map(this::toResponse).toList();
    }

    public void restore(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
        project.setDeletedAt(null);
        projectRepository.save(project);
    }

    public List<WorkloadResponse> getWorkload(Long projectId) {
        findOrThrow(projectId);
        List<User> assignees = ticketRepository.findAssigneesByProjectId(projectId);
        return assignees.stream().map(user -> {
            long count = ticketRepository.countByAssigneeIdAndStatusNotAndDeletedAtIsNull(user.getId(), TicketStatus.DONE);
            return WorkloadResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .openTicketCount(count)
                    .build();
        }).toList();
    }

    public Project findOrThrow(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
    }

    public ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .build();
    }
}
