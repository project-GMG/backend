-- 식당(RESTAURANT) 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES (1, '한식', 'KOREAN_FOOD', CURRENT_TIMESTAMP),
       (1, '중식', 'CHINESE_FOOD', CURRENT_TIMESTAMP),
       (1, '일식', 'JAPANESE_FOOD', CURRENT_TIMESTAMP),
       (1, '양식', 'WESTERN_FOOD', CURRENT_TIMESTAMP),
       (1, '분식·야식', 'SNACK_FOOD', CURRENT_TIMESTAMP);

-- 카페(CAFE) 카테고리 (임시)
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES (2, 'A', 'CAFE_A', CURRENT_TIMESTAMP),
       (2, 'B', 'CAFE_B', CURRENT_TIMESTAMP),
       (2, 'C', 'CAFE_C', CURRENT_TIMESTAMP);

-- 술집(BAR) 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES (3, '소주·맥주', 'SOJU_BEER', CURRENT_TIMESTAMP),
       (3, '이자카야', 'IZAKAYA', CURRENT_TIMESTAMP),
       (3, '막걸리', 'MAKGEOLLI', CURRENT_TIMESTAMP),
       (3, '펍·칵테일', 'PUB_COCKTAIL', CURRENT_TIMESTAMP),
       (3, '와인', 'WINE', CURRENT_TIMESTAMP);

-- 스터디(STUDY) 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES (4, '독서실', 'STUDY_ROOM', CURRENT_TIMESTAMP),
       (4, '스터디카페', 'STUDY_CAFE', CURRENT_TIMESTAMP),
       (4, '도서관', 'LIBRARY', CURRENT_TIMESTAMP),
       (4, '스터디룸', 'STUDY_LOUNGE', CURRENT_TIMESTAMP);