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
- Do not add new dependencies to pom.xml
- Replace the placeholder schema.sql with the real schema
- application.yaml is already configured for PostgreSQL on localhost:5432
- Test profile uses H2 (see src/test/resources/application.yaml)