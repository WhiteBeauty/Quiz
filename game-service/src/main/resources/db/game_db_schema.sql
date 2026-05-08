CREATE TABLE IF NOT EXISTS game_rooms (
    id BIGSERIAL PRIMARY KEY,
    room_code VARCHAR(10) NOT NULL UNIQUE,
    quiz_id BIGINT NOT NULL,
    quiz_title VARCHAR(200) NOT NULL,
    question_count INTEGER NOT NULL,
    host_username VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    current_question_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_players (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES game_rooms(id) ON DELETE CASCADE,
    username VARCHAR(50) NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(room_id, username)
);

CREATE TABLE IF NOT EXISTS game_answers (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES game_rooms(id) ON DELETE CASCADE,
    player_id BIGINT NOT NULL REFERENCES game_players(id) ON DELETE CASCADE,
    question_index INTEGER NOT NULL,
    answer_option_id BIGINT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    points_earned INTEGER NOT NULL DEFAULT 0,
    answer_time_ms BIGINT NOT NULL,
    answered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_game_rooms_code ON game_rooms(room_code);
CREATE INDEX IF NOT EXISTS idx_game_players_room ON game_players(room_id);
CREATE INDEX IF NOT EXISTS idx_game_answers_room ON game_answers(room_id);
