CREATE TABLE user_role
(
    role_id BIGINT NOT NULL,
    user_id UUID   NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (role_id, user_id)
);

CREATE TABLE user
(
    id                      UUID    NOT NULL,
    google_id               VARCHAR(255),
    email                   VARCHAR(255),
    name                    VARCHAR(255),
    verification_token_id   UUID,
    newsletter_subscription BOOLEAN NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE user
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE user
    ADD CONSTRAINT uc_user_googleid UNIQUE (google_id);

ALTER TABLE user
    ADD CONSTRAINT FK_USER_ON_VERIFICATIONTOKEN FOREIGN KEY (verification_token_id) REFERENCES verification_token (id);

ALTER TABLE user_role
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE user_role
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user (id);