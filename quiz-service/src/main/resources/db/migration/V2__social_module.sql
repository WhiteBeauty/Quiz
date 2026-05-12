-- Социальный модуль: друзья, чат, приглашения в игру (при включённом Flyway)
CREATE TABLE IF NOT EXISTS friendships (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user2_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    initiator_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_friendships_pair UNIQUE (user1_id, user2_id),
    CONSTRAINT chk_friendships_order CHECK (user1_id < user2_id)
);

CREATE INDEX IF NOT EXISTS idx_friendships_user1_status ON friendships (user1_id, status);
CREATE INDEX IF NOT EXISTS idx_friendships_user2_status ON friendships (user2_id, status);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    recipient_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    delivered_at TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_pair_created ON chat_messages (sender_id, recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_recipient_created ON chat_messages (recipient_id, sender_id, created_at DESC);

CREATE TABLE IF NOT EXISTS game_invites (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    inviter_user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    invitee_user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    invite_type VARCHAR(30) NOT NULL,
    room_code VARCHAR(64),
    quiz_id BIGINT,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_game_invites_invitee ON game_invites (invitee_user_id);
