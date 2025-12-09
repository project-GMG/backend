-- Event Place Types 중간 테이블 생성
CREATE TABLE event_place_types (
                                   id BIGINT AUTO_INCREMENT NOT NULL,
                                   event_id BIGINT NOT NULL,
                                   place_type_id BIGINT NOT NULL,
                                   selected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT pk_event_place_types PRIMARY KEY (id),
    -- event삭제시 event_place_type도 삭제
                                   CONSTRAINT fk_event_place_types_event
                                       FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_event_place_types_place_type
                                       FOREIGN KEY (place_type_id) REFERENCES place_types(id) ON DELETE RESTRICT
);

-- 인덱스 생성
CREATE INDEX idx_event_place_type_event_id ON event_place_types(event_id);
CREATE UNIQUE INDEX idx_event_place_type ON event_place_types(event_id, place_type_id);