
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    time_limit_seconds INTEGER NOT NULL DEFAULT 30,
    order_index INTEGER NOT NULL,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS answer_options (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(300) NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT FALSE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE
);

-- Индексы для ускорения запросов
CREATE INDEX IF NOT EXISTS idx_quizzes_author ON quizzes(author_id);
CREATE INDEX IF NOT EXISTS idx_questions_quiz ON questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_answer_options_question ON answer_options(question_id);
