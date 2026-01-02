-- 1. 기존 샘플데이터 삭제
DELETE FROM places;

-- 2. 새로운 컬럼 추가
ALTER TABLE places ADD COLUMN kakao_place_id VARCHAR(50) NULL COMMENT '카카오맵 API 장소 ID';
ALTER TABLE places ADD COLUMN provider VARCHAR(20) NULL DEFAULT 'KAKAO' COMMENT 'KAKAO, NAVER';

-- 3. UNIQUE 제약 조건, 인덱스 추가
ALTER TABLE places ADD CONSTRAINT uk_place_kakao_id UNIQUE (kakao_place_id);

CREATE INDEX idx_place_kakao_id ON places(kakao_place_id);
CREATE INDEX idx_place_provider ON places(provider);