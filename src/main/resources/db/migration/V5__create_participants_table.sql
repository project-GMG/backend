CREATE TABLE participants
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    event_id     BIGINT      NOT NULL,
    name         VARCHAR(50) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    joined_at    DATETIME    NOT NULL,
    completed_at DATETIME    NULL,

    CONSTRAINT pk_participants PRIMARY KEY (id)
);

ALTER TABLE participants
    ADD CONSTRAINT fk_participants_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;

CREATE INDEX idx_event_participant
    ON participants (event_id, name);

CREATE INDEX idx_participants_status
    ON participants (status);