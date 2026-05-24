# IssueFlow – Setup, Build & Run

## Prerequisites
- Java 21+
- Maven (or use `./mvnw` wrapper — no separate install needed)
- Docker & Docker Compose (for PostgreSQL)

## 1. Start the Database

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with database `issueflow`, user `issueflow`, password `issueflow`.

## 2. Build

```bash
./mvnw clean package -DskipTests
```

Maven downloads all dependencies automatically on first run.

## 3. Run the Application

```bash
./mvnw spring-boot:run
```

Or run the packaged jar:

```bash
java -jar target/issueflow-*.jar
```

The API is available at `http://localhost:8080`.

## 4. Authenticate

All endpoints except `/auth/**` require a JWT Bearer token.

**Step 1 – Create a user:**
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","fullName":"Admin User","role":"ADMIN","password":"secret"}'
```

**Step 2 – Login to get a token:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}'
```

Response:
```json
{ "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 3600 }
```

**Step 3 – Use the token in all subsequent requests:**
```bash
curl http://localhost:8080/projects \
  -H "Authorization: Bearer <jwt>"
```

## 5. Run Tests

Tests use an in-memory H2 database — no Docker or authentication needed:

```bash
./mvnw test
```

## API Quick Reference

| Feature        | Base Path                        | Auth Required |
|----------------|----------------------------------|---------------|
| Auth           | `/auth`                          | No            |
| Users          | `/users`                         | Yes           |
| Projects       | `/projects`                      | Yes           |
| Tickets        | `/tickets`                       | Yes           |
| Comments       | `/tickets/:id/comments`          | Yes           |
| Dependencies   | `/tickets/:id/dependencies`      | Yes           |
| Attachments    | `/tickets/:id/attachments`       | Yes           |
| Audit Logs     | `/audit-logs`                    | Yes           |
| Mentions       | `/users/:id/mentions`            | Yes           |
| Workload       | `/projects/:id/workload`         | Yes           |

> **Note:** `/tickets/deleted`, `/projects/deleted`, and all `/restore` endpoints require ADMIN role.
