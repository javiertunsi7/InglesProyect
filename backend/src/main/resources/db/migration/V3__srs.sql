-- Spaced Repetition System (SM-2 simplificado).
-- Cada user_question_attempt incorpora su propio scheduler: cuándo vuelve
-- a aparecer la pregunta y con qué dificultad. La sesión diaria se arma
-- pidiendo las que tienen next_review_date <= hoy.

ALTER TABLE user_question_attempts
    ADD COLUMN repetitions       INT          NOT NULL DEFAULT 0;

ALTER TABLE user_question_attempts
    ADD COLUMN ease_factor       NUMERIC(3,2) NOT NULL DEFAULT 2.50;

ALTER TABLE user_question_attempts
    ADD COLUMN interval_days     INT          NOT NULL DEFAULT 0;

ALTER TABLE user_question_attempts
    ADD COLUMN next_review_date  DATE;

-- Backfill: las filas existentes se marcan como debidas hoy para que
-- entren en la próxima sesión SRS y empiecen a engancharse al ciclo.
UPDATE user_question_attempts
   SET next_review_date = CURRENT_DATE
 WHERE next_review_date IS NULL;

ALTER TABLE user_question_attempts
    ALTER COLUMN next_review_date SET NOT NULL;

CREATE INDEX idx_uqa_user_next_review
    ON user_question_attempts (user_id, next_review_date);
