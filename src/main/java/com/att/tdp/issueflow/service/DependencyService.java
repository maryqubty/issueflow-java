package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.request.AddDependencyRequest;
import com.att.tdp.issueflow.dto.response.DependencyResponse;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.exception.ConflictException;
import com.att.tdp.issueflow.exception.ValidationException;
import com.att.tdp.issueflow.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DependencyService {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    @Transactional
    public void addDependency(Long ticketId, AddDependencyRequest req) {
        Long blockerId = req.getBlockedBy();
        if (ticketId.equals(blockerId)) {
            throw new ValidationException("A ticket cannot block itself");
        }
        Ticket ticket = ticketService.findOrThrow(ticketId);
        Ticket blocker = ticketService.findOrThrow(blockerId);

        if (!ticket.getProject().getId().equals(blocker.getProject().getId())) {
            throw new ValidationException("Both tickets must belong to the same project");
        }

        boolean exists = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM ticket_dependencies WHERE ticket_id = ? AND blocked_by_id = ?")
                .setParameter(1, ticketId)
                .setParameter(2, blockerId)
                .getSingleResult()).intValue() > 0;

        if (exists) {
            throw new ConflictException("Dependency already exists");
        }

        entityManager.createNativeQuery(
                "INSERT INTO ticket_dependencies (ticket_id, blocked_by_id) VALUES (?, ?)")
                .setParameter(1, ticketId)
                .setParameter(2, blockerId)
                .executeUpdate();
    }

    public List<DependencyResponse> getDependencies(Long ticketId) {
        ticketService.findOrThrow(ticketId);
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT t.id, t.title, t.status FROM tickets t " +
                "JOIN ticket_dependencies d ON t.id = d.blocked_by_id " +
                "WHERE d.ticket_id = ? AND t.deleted_at IS NULL")
                .setParameter(1, ticketId)
                .getResultList();

        return rows.stream().map(row -> DependencyResponse.builder()
                .id(((Number) row[0]).longValue())
                .title((String) row[1])
                .status(com.att.tdp.issueflow.enums.TicketStatus.valueOf((String) row[2]))
                .build()).toList();
    }

    @Transactional
    public void removeDependency(Long ticketId, Long blockerId) {
        ticketService.findOrThrow(ticketId);
        entityManager.createNativeQuery(
                "DELETE FROM ticket_dependencies WHERE ticket_id = ? AND blocked_by_id = ?")
                .setParameter(1, ticketId)
                .setParameter(2, blockerId)
                .executeUpdate();
    }
}
