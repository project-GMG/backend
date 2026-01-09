ALTER TABLE events
    ADD COLUMN place_search_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 기존 데이터는 COMPLETED로 설정 (이미 장소 검색이 완료된 상태라고 가정)
UPDATE events
SET place_search_status = 'COMPLETED'
WHERE place_search_status = 'PENDING';

-- 인덱스 추가 (상태별 조회가 필요한 경우)
-- CREATE INDEX idx_events_place_search_status ON events(place_search_status);