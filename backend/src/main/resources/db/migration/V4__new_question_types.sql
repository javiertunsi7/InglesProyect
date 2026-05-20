-- Add columns for Phase 5 new question types
-- LISTENING / DICTATION: audio_text holds the text sent to TTS
-- MATCHING: match_group identifies pairs in question_options

ALTER TABLE questions ADD COLUMN IF NOT EXISTS audio_text VARCHAR(255);

ALTER TABLE question_options ADD COLUMN IF NOT EXISTS match_group VARCHAR(32);
