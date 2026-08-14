-- Newsletter Service schema: newsletter_subscriber
-- No user_id FK - Newsletter Service is decoupled from User/Auth

CREATE TABLE IF NOT EXISTS newsletter_subscriber (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unsubscribed BOOLEAN NOT NULL DEFAULT FALSE,
    unsubscribed_time TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_newsletter_subscriber_email ON newsletter_subscriber(email);
CREATE INDEX IF NOT EXISTS idx_newsletter_subscriber_unsubscribed ON newsletter_subscriber(unsubscribed);
