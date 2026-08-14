-- Membership Service schema: membership table
-- No FK to mmhc_user; links via optional user_id (UUID) and required email

CREATE TABLE IF NOT EXISTS membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    membership_type VARCHAR(30) NOT NULL,
    student_id VARCHAR(100),
    instagram VARCHAR(255),
    instagram_groupchat BOOLEAN DEFAULT FALSE,
    newsletter_opt_in BOOLEAN DEFAULT FALSE,
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    stripe_session_id VARCHAR(255),
    payment_status VARCHAR(50),
    payment_method VARCHAR(30),
    approved_by VARCHAR(255),
    verified_at TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    active BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_membership_email ON membership(email);
CREATE INDEX IF NOT EXISTS idx_membership_user_id ON membership(user_id);
CREATE INDEX IF NOT EXISTS idx_membership_active_payment ON membership(active, payment_status);
