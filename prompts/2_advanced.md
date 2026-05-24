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