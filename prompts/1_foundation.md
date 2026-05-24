Read README.md and CLAUDE.md, then implement the foundation.

1. Replace schema.sql with the real schema for all entities
2. JPA Entities: User, Project, Ticket, Comment, AuditLog, Dependency, Attachment
   - proper relationships and enums (Role, Status, Priority, TicketType)
   - soft-delete fields (deletedAt) on Ticket and Project
3. Repositories
4. DTOs (request + response) with Bean Validation annotations
5. Services and Controllers for: Users, Projects, Tickets
   - exact endpoints from README.md, nothing more nothing less
6. Global @ControllerAdvice exception handler

When done, run ./mvnw test and fix any failures before stopping.