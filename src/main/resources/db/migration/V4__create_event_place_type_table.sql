CREATE TABLE event_place_types (
                                   id BIGINT AUTO_INCREMENT NOT NULL,
                                   event_id BIGINT NOT NULL,
                                   place_type_id BIGINT NOT NULL,
                                   selected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT pk_event_place_types PRIMARY KEY (id)
);

ALTER TABLE event_place_types
    ADD CONSTRAINT fk_event_place_types_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;

ALTER TABLE event_place_types
    ADD CONSTRAINT fk_event_place_types_place_type
        FOREIGN KEY (place_type_id) REFERENCES place_types (id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX idx_event_place_type
    ON event_place_types (event_id, place_type_id);