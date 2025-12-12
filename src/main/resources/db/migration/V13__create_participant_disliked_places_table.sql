CREATE TABLE participant_disliked_places
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    event_id       BIGINT    NOT NULL,
    participant_id BIGINT    NOT NULL,
    place_id       BIGINT    NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL,

    CONSTRAINT pk_participant_disliked_places PRIMARY KEY (id)
);

ALTER TABLE participant_disliked_places
    ADD CONSTRAINT fk_participant_disliked_places_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;

ALTER TABLE participant_disliked_places
    ADD CONSTRAINT fk_participant_disliked_places_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id) ON DELETE CASCADE;

ALTER TABLE participant_disliked_places
    ADD CONSTRAINT fk_participant_disliked_places_place
        FOREIGN KEY (place_id) REFERENCES places (id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX idx_participant_disliked_place
    ON participant_disliked_places (participant_id, place_id);