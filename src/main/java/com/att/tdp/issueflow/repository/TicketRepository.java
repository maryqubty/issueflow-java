package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByProjectIdAndDeletedAtIsNull(Long projectId);
    List<Ticket> findByProjectIdAndDeletedAtIsNotNull(Long projectId);
    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);
    List<Ticket> findByDueDateBeforeAndStatusNotAndDeletedAtIsNull(LocalDateTime now, TicketStatus status);
    long countByAssigneeIdAndStatusNotAndDeletedAtIsNull(Long assigneeId, TicketStatus status);
    long countByAssigneeIdAndProjectIdAndStatusNotAndDeletedAtIsNull(Long assigneeId, Long projectId, TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.project.id = :projectId AND t.deletedAt IS NULL AND t.status != :status")
    List<Ticket> findOpenTicketsByProject(@Param("projectId") Long projectId, @Param("status") TicketStatus status);

    @Query("SELECT DISTINCT t.assignee FROM Ticket t WHERE t.project.id = :projectId AND t.assignee IS NOT NULL AND t.deletedAt IS NULL")
    List<com.att.tdp.issueflow.entity.User> findAssigneesByProjectId(@Param("projectId") Long projectId);

    @Query(value = "SELECT COUNT(*) FROM ticket_dependencies d JOIN tickets t ON t.id = d.blocked_by_id WHERE d.ticket_id = :ticketId AND t.status != 'DONE' AND t.deleted_at IS NULL", nativeQuery = true)
    long countUnresolvedBlockers(@Param("ticketId") Long ticketId);
}
