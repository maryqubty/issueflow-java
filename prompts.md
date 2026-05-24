# Prompts – AI Agent Interaction

**Model used:** Claude Sonnet 4.6 (`claude-sonnet-4-6`) via Claude Code CLI

This file documents how the AI agent (Claude Code) was used to implement IssueFlow.

---

## Agent Setup – CLAUDE.md

A `CLAUDE.md` file was created in the project root to give the agent persistent context about the stack, conventions, and constraints. This was loaded automatically at the start of every session.

```markdown
# IssueFlow Project Context

## Stack
- Java 21, Spring Boot 3.4.2, Maven
- PostgreSQL (prod), H2 (tests)
- Spring Data JPA + Hibernate
- Lombok (use it everywhere to reduce boilerplate)
- Bean Validation (jakarta.validation)
- Apache Commons CSV (already in pom.xml)

## Conventions
- Constructor injection, never @Autowired field injection
- DTOs for all request/response bodies — never expose entities directly
- All business logic in services, controllers are thin
- Use @ControllerAdvice for all error handling
- All endpoints must exactly match README.md (method, path, request body, response body)
- meaningful HTTP status codes (404 for not found, 400 for validation errors, 409 for conflicts)

## Database
- Do not add dependencies unless they are strictly necessary and not already covered by the existing ones
- Replace the placeholder schema.sql with the real schema
- application.yaml is already configured for PostgreSQL on localhost:5432
- Test profile uses H2 (see src/test/resources/application.yaml)
```

---

## Prompt 1 – Foundation

**File:** `prompts/1_foundation.md`

```
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
```

**What the agent built:**
- `schema.sql` with all 9 tables
- 7 JPA entities with Lombok and proper relationships
- 7 enums (Role, TicketStatus, Priority, TicketType, AuditAction, EntityType, Actor)
- 8 Spring Data JPA repositories with custom query methods
- ~20 request/response DTOs with Bean Validation
- UserController, ProjectController, TicketController
- UserService, ProjectService, TicketService
- GlobalExceptionHandler with 404 / 409 / 400 / 500 mappings

---

## Prompt 2 – Advanced Features

**File:** `prompts/2_advanced.md`

```
The base CRUD is working. Now implement:

1. Comments API with @mention parsing
   - parse @username from comment content
   - validate each mentioned username exists
   - persist mentions, return mentionedUsers list in response
2. Dependencies API (ticket-to-ticket blocker relationships)
3. Attachments API
4. Audit Log
   - auto-record CREATE/UPDATE/DELETE for Ticket, Project, Comment
   - fields: action, entityType, entityId, performedBy, actor, timestamp
5. CSV export: GET /tickets/export?projectId=
6. CSV import: POST /tickets/import (multipart, projectId form field)
   - return { created, failed, errors }
7. Soft delete + restore for Ticket and Project (restore for ADMIN only)

Run ./mvnw test and fix any failures before stopping.
```

**What the agent built:**
- CommentService + CommentController with regex-based `@mention` parsing
- DependencyService + DependencyController
- AttachmentService + AttachmentController (raw bytes stored in DB)
- AuditLogService called from every service on CREATE/UPDATE/DELETE
- AuditLogController with optional query param filtering
- CSV export via Apache Commons CSV (`text/csv` response)
- CSV import with per-row error collection and `ImportResultResponse`
- Soft delete + restore endpoints for both Ticket and Project
- WorkloadAPI and MentionsAPI

---

## Prompt 3 – Auth, Scheduler & Tests

**File:** `prompts/3_auth_test.md`

```
Final layer. Implement:

1. JWT Authentication
   - POST /auth/login → returns { accessToken, tokenType, expiresIn }
   - POST /auth/logout → invalidates token
   - GET /auth/me → returns current user
   - Security filter that protects all routes

2. Auto-assignment
   - When a ticket is created with no assignee, assign to the DEVELOPER
     in that project with the fewest non-DONE tickets
   
3. Auto-escalation scheduler
   - Runs on a schedule (every hour is fine)
   - Finds tickets where dueDate is past and status != DONE
   - Bumps their priority one level up (LOW→MEDIUM→HIGH→CRITICAL)

4. Integration tests using H2 for:
   - Users API (CRUD)
   - Projects API (CRUD + soft delete)
   - Tickets API (CRUD + export/import)

Run ./mvnw test and fix all failures before stopping.
```

**What the agent built:**
- JwtService (token generation + validation using jjwt 0.12.x)
- JwtAuthFilter (OncePerRequestFilter, checks revoked tokens)
- SecurityConfig (stateless, permits /auth/**)
- UserDetailsServiceImpl (standalone @Service, no circular dependency)
- PasswordEncoderConfig (separate class to avoid circular dependency)
- AuthController + AuthService (login, logout, /me)
- Auto-assignment logic inside TicketService.create()
- EscalationScheduler with @Scheduled(fixedRate = 3600000)
- UserControllerTest, ProjectControllerTest, TicketControllerTest (16 tests total)
- TestSecurityConfig bypassing JWT in tests via @ConditionalOnMissingBean

**Final result:** 16/16 integration tests passing.
