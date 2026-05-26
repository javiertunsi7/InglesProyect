-- Columnas para flujo "olvidé mi contraseña".
-- Una por ALTER porque H2 no acepta múltiples ADD COLUMN IF NOT EXISTS en un
-- solo statement (ver V5).
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token_expiry TIMESTAMP;
