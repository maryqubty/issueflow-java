package com.att.tdp.issueflow.scheduler;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import com.att.tdp.issueflow.enums.Priority;
import com.att.tdp.issueflow.enums.TicketStatus;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscalationScheduler {

    private final TicketRepository ticketRepository;
    private final AuditLogService auditLogService;

    @Scheduled(fixedRate = 3_600_000)
    public void escalateOverdueTickets() {
        List<Ticket> overdue = ticketRepository.findByDueDateBeforeAndStatusNotAndDeletedAtIsNull(
                LocalDateTime.now(), TicketStatus.DONE);

        for (Ticket ticket : overdue) {
            Priority current = ticket.getPriority();
            Priority next = escalate(current);
            ticket.setPriority(next);
            if (next == Priority.CRITICAL) {
                ticket.setOverdue(true);
            }
            ticketRepository.save(ticket);
            auditLogService.record(AuditAction.UPDATE, EntityType.TICKET, ticket.getId(), null, Actor.SYSTEM);
            log.info("Escalated ticket {} from {} to {}", ticket.getId(), current, next);
        }
    }

    private Priority escalate(Priority p) {
        return switch (p) {
            case LOW -> Priority.MEDIUM;
            case MEDIUM -> Priority.HIGH;
            case HIGH -> Priority.CRITICAL;
            case CRITICAL -> Priority.CRITICAL;
        };
    }
}
