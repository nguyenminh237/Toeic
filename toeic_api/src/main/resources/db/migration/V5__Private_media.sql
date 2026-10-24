CREATE TABLE private_media (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    storage_key VARCHAR(80) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_private_media_owner FOREIGN KEY(owner_id) REFERENCES users(id)
);
CREATE INDEX ix_private_media_owner ON private_media(owner_id,id);
