-- 1. 중복 데이터 정리
DELETE FROM participants
WHERE id NOT IN (
    SELECT * FROM (
                      SELECT MIN(id)
                      FROM participants
                      GROUP BY event_id, name
                  ) AS keep_ids
);

-- 2. 외래 키 제약 조건 임시 삭제
ALTER TABLE participants DROP FOREIGN KEY fk_participants_event;

-- 3. 기존 인덱스 삭제 (이제 가능)
DROP INDEX idx_event_participant ON participants;

-- 4. 유니크 인덱스 생성
CREATE UNIQUE INDEX uk_event_name ON participants (event_id, name);

-- 5. 외래 키 제약 조건 다시 생성
ALTER TABLE participants
    ADD CONSTRAINT fk_participants_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;