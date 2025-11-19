CREATE TABLE user_roles
(
    roles_id BIGINT NOT NULL,
    users_id UUID   NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (roles_id, users_id)
);

CREATE TABLE users
(
    id                      UUID    NOT NULL,
    google_id               VARCHAR(255),
    email                   VARCHAR(255),
    name                    VARCHAR(255),
    verification_token_id   UUID,
    newsletter_subscription BOOLEAN NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_googleid UNIQUE (google_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_VERIFICATIONTOKEN FOREIGN KEY (verification_token_id) REFERENCES verification_token (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (roles_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (users_id) REFERENCES users (id);