-- User profile: newsletter subscription flag (owned by User Service, column in shared auth_db)
ALTER TABLE mmhc_user ADD COLUMN IF NOT EXISTS newsletter_subscription BOOLEAN NOT NULL DEFAULT FALSE;
