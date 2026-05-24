# IssueFlow – Setup, Build & Run

## Prerequisites
- Java 21+
- Maven (or use `./mvnw`)
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

## 3. Run the Application

```bash
./mvnw spring-boot:run
```

Or run the packaged jar:

```bash
java -jar target/issueflow-*.jar
```

The API is available at `http://localhost:8080`.

## 4. Run Tests

Tests use an in-memory H2 database (no Docker needed):

```bash
./mvnw test
```

## API Quick Reference

| Feature | Base Path |
|---------|-----------|
| Users | `/users` |
| Projects | `/projects` |
| Tickets | `/tickets` |
| Comments | `/tickets/:id/comments` |
| Dependencies | `/tickets/:id/dependencies` |
| Attachments | `/tickets/:id/attachments` |
| Audit Logs | `/audit-logs` |
| Mentions | `/users/:id/mentions` |
| Workload | `/projects/:id/workload` |
