-- =====================================================
-- Flyway Migration V1: Initial Schema
-- Generated from JPA Entities
-- =====================================================

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE users (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    updated_at DATETIME(6),
    auth_provider VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255),
    profile_image_path VARCHAR(1024),
    role VARCHAR(20) NOT NULL,
    locked BIT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_deleted_at ON users (deleted_at);

-- -----------------------------------------------------
-- Table: contents
-- -----------------------------------------------------
CREATE TABLE contents (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    updated_at DATETIME(6),
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    thumbnail_path VARCHAR(1024) NOT NULL,
    review_count INT NOT NULL,
    average_rating DOUBLE NOT NULL,
    popularity_score DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contents_deleted_at ON contents (deleted_at);

-- -----------------------------------------------------
-- Table: tags
-- -----------------------------------------------------
CREATE TABLE tags (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    name VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: genres
-- -----------------------------------------------------
CREATE TABLE genres (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    tmdb_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_genres_tmdb_id (tmdb_id),
    UNIQUE KEY uk_genres_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: leagues
-- -----------------------------------------------------
CREATE TABLE leagues (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    league_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sport VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_leagues_league_id (league_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: content_tags
-- -----------------------------------------------------
CREATE TABLE content_tags (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    content_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_content_tags_content_tag (content_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_content_tags_tag_id ON content_tags (tag_id);

-- -----------------------------------------------------
-- Table: content_external_mappings
-- -----------------------------------------------------
CREATE TABLE content_external_mappings (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    provider VARCHAR(30) NOT NULL,
    external_id BIGINT NOT NULL,
    content_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_content_external_mappings_provider_external (provider, external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: content_deletion_logs
-- -----------------------------------------------------
CREATE TABLE content_deletion_logs (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    content_id BINARY(16) NOT NULL,
    thumbnail_path VARCHAR(1024) NOT NULL,
    image_processed_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_content_deletion_logs_content_id (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: playlists
-- -----------------------------------------------------
CREATE TABLE playlists (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    updated_at DATETIME(6),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id BINARY(16) NOT NULL,
    subscriber_count INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_playlists_owner_updated_at ON playlists (owner_id, updated_at DESC);

-- -----------------------------------------------------
-- Table: playlist_contents
-- -----------------------------------------------------
CREATE TABLE playlist_contents (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    playlist_id BINARY(16) NOT NULL,
    content_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_playlist_contents_playlist_content (playlist_id, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: playlist_subscribers
-- -----------------------------------------------------
CREATE TABLE playlist_subscribers (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    playlist_id BINARY(16) NOT NULL,
    subscriber_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_playlist_subscribers_playlist_subscriber (playlist_id, subscriber_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_playlist_subscribers_subscriber_id ON playlist_subscribers (subscriber_id);

-- -----------------------------------------------------
-- Table: reviews
-- -----------------------------------------------------
CREATE TABLE reviews (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    updated_at DATETIME(6),
    text TEXT,
    rating DOUBLE NOT NULL,
    content_id BINARY(16) NOT NULL,
    author_id BINARY(16),
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_content_author (content_id, author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_reviews_content_created_at ON reviews (content_id, created_at DESC);
CREATE INDEX idx_reviews_content_rating ON reviews (content_id, rating DESC);

-- -----------------------------------------------------
-- Table: follows
-- -----------------------------------------------------
CREATE TABLE follows (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    follower_id BINARY(16) NOT NULL,
    followee_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_follows_follower_followee (follower_id, followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_follows_followee_id ON follows (followee_id);

-- -----------------------------------------------------
-- Table: notifications
-- -----------------------------------------------------
CREATE TABLE notifications (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    title VARCHAR(500) NOT NULL,
    content TEXT,
    level VARCHAR(20) NOT NULL,
    receiver_id BINARY(16) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_receiver_created_at ON notifications (receiver_id, created_at DESC);

-- -----------------------------------------------------
-- Table: conversations
-- -----------------------------------------------------
CREATE TABLE conversations (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: direct_messages
-- -----------------------------------------------------
CREATE TABLE direct_messages (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    content TEXT,
    sender_id BINARY(16),
    conversation_id BINARY(16) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_direct_messages_conversation_created_at ON direct_messages (conversation_id, created_at DESC);

-- -----------------------------------------------------
-- Table: read_statuses
-- -----------------------------------------------------
CREATE TABLE read_statuses (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    last_read_at DATETIME(6) NOT NULL,
    participant_id BINARY(16) NOT NULL,
    conversation_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_read_statuses_participant_conversation (participant_id, conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_read_statuses_conversation_id ON read_statuses (conversation_id);

-- -----------------------------------------------------
-- Table: outbox_events
-- -----------------------------------------------------
CREATE TABLE outbox_events (
    id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    published_at DATETIME(6),
    retry_count INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_outbox_status_created ON outbox_events (status, created_at);

-- -----------------------------------------------------
-- Table: system_settings
-- -----------------------------------------------------
CREATE TABLE system_settings (
    setting_key VARCHAR(100) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
