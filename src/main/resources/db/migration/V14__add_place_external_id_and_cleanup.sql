-- 1. 기존 샘플데이터 삭제
DELETE FROM places;

-- 2. 새로운 컬럼 추가
ALTER TABLE places ADD COLUMN place_external_id VARCHAR(50) NULL COMMENT '외부 장소 ID (카카오맵, 네이버 등)';
ALTER TABLE places ADD COLUMN provider VARCHAR(20) NULL DEFAULT 'KAKAO' COMMENT 'KAKAO, NAVER';

-- 3. UNIQUE 제약 조건, 인덱스 추가
ALTER TABLE places ADD CONSTRAINT uk_place_external_id UNIQUE (place_external_id);

CREATE INDEX idx_place_external_id ON places(place_external_id);
CREATE INDEX idx_place_provider ON places(provider);