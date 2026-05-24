CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    project_id BIGINT REFERENCES projects(id),
    assignee_id BIGINT REFERENCES users(id),
    due_date TIMESTAMP,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES tickets(id),
    author_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS comment_mentions (
    comment_id BIGINT REFERENCES comments(id),
    user_id BIGINT REFERENCES users(id),
    PRIMARY KEY (comment_id, user_id)
);

CREATE TABLE IF NOT EXISTS ticket_dependencies (
    ticket_id BIGINT REFERENCES tickets(id),
    blocked_by_id BIGINT REFERENCES tickets(id),
    PRIMARY KEY (ticket_id, blocked_by_id)
);

CREATE TABLE IF NOT EXISTS attachments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES tickets(id),
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    data BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    performed_by BIGINT,
    actor VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS revoked_tokens (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL,
    revoked_at TIMESTAMP NOT NULL
);
