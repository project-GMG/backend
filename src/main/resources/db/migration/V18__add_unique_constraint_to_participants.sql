-- 기존 중복 데이터 정리
DELETE p1 FROM participants p1
INNER JOIN participants p2 
WHERE p1.id > p2.id 
  AND p1.event_id = p2.event_id 
  AND p1.name = p2.name;

-- 기존 인덱스 삭제
DROP INDEX idx_event_participant ON participants;

-- 유니크 인덱스 추가
CREATE UNIQUE INDEX uk_event_name ON participants (event_id, name);