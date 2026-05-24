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