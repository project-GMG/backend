CREATE TABLE participant_unavailable_times
(
    id                     BIGINT AUTO_INCREMENT NOT NULL,
    event_id               BIGINT   NOT NULL,
    participant_id         BIGINT   NOT NULL,
    unavailable_date       DATE     NOT NULL,
    unavailable_time_start TIME     NOT NULL,
    unavailable_time_end   TIME     NOT NULL,
    created_at             DATETIME NOT NULL,

    CONSTRAINT pk_participant_unavailable_times PRIMARY KEY (id)
);

ALTER TABLE participant_unavailable_times
    ADD CONSTRAINT fk_participant_unavailable_times_on_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;

ALTER TABLE participant_unavailable_times
    ADD CONSTRAINT fk_participant_unavailable_times_on_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id) ON DELETE CASCADE;

CREATE INDEX idx_event_date_time
    ON participant_unavailable_times (event_id, unavailable_date, unavailable_time_start);

CREATE INDEX idx_participant_date
    ON participant_unavailable_times (participant_id, unavailable_date);