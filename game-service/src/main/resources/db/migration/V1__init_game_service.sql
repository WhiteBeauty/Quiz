CREATE TABLE IF NOT EXISTS game_rooms (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    quiz_id BIGINT NOT NULL,
    host_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    current_question_index INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS game_participants (
    id BIGSERIAL PRIMARY KEY,
    game_room_id BIGINT NOT NULL REFERENCES game_rooms(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    total_score INT NOT NULL DEFAULT 0,
    connected BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS player_answers (
    id BIGSERIAL PRIMARY KEY,
    game_room_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL REFERENCES game_participants(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    correct BOOLEAN NOT NULL DEFAULT FALSE,
    points_earned INT NOT NULL DEFAULT 0,
    answered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    response_time_ms BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS game_results (
    id BIGSERIAL PRIMARY KEY,
    game_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    final_score INT NOT NULL DEFAULT 0,
    rank INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_game_rooms_code ON game_rooms(code);
CREATE INDEX IF NOT EXISTS idx_game_participants_room ON game_participants(game_room_id);
CREATE INDEX IF NOT EXISTS idx_player_answers_participant ON player_answers(participant_id);
CREATE INDEX IF NOT EXISTS idx_game_results_user ON game_results(user_id);
