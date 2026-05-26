-- Perfil extendido del usuario.
-- H2 (incluso en MODE=PostgreSQL) no permite múltiples ADD COLUMN dentro de un
-- mismo ALTER cuando se usa IF NOT EXISTS, así que partimos el statement.
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(512);
ALTER TABLE users ADD COLUMN IF NOT EXISTS daily_goal_minutes INT DEFAULT 15 NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS daily_goal_xp INT DEFAULT 50 NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
