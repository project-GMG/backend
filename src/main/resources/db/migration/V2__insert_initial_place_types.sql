-- 장소 타입 초기 데이터 삽입
INSERT INTO place_types (code, label, created_at)
VALUES ('RESTAURANT', '식당', CURRENT_TIMESTAMP),
       ('CAFE', '카페', CURRENT_TIMESTAMP),
       ('BAR', '술집', CURRENT_TIMESTAMP),
       ('STUDY', '스터디', CURRENT_TIMESTAMP);