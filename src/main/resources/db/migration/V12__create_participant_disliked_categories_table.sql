CREATE TABLE participant_disliked_categories
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    event_id       BIGINT    NOT NULL,
    participant_id BIGINT    NOT NULL,
    category_id    BIGINT    NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL,

    CONSTRAINT pk_participant_disliked_categories PRIMARY KEY (id)
);

ALTER TABLE participant_disliked_categories
    ADD CONSTRAINT fk_participant_disliked_categories_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;

ALTER TABLE participant_disliked_categories
    ADD CONSTRAINT fk_participant_disliked_categories_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id) ON DELETE CASCADE;

ALTER TABLE participant_disliked_categories
    ADD CONSTRAINT fk_participant_disliked_categories_category
        FOREIGN KEY (category_id) REFERENCES place_categories (id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX idx_participant_disliked_category
    ON participant_disliked_categories (participant_id, category_id);