-- 키워드 기반 검색으로 통합하기 위한 카테고리 재구성
-- RESTAURANT: 9개, CAFE: 4개, BAR: 4개, STUDY: 2개로 변경

-- Step 1: Foreign Key 제약으로 인한 삭제 오류 방지
UPDATE places SET category_id = NULL WHERE category_id IS NOT NULL;

-- Step 2: 참여자 싫어요 카테고리 초기화 (사용자는 재설정 필요)
DELETE FROM participant_disliked_categories WHERE category_id IS NOT NULL;

-- Step 3: 기존 카테고리 삭제
DELETE FROM place_categories WHERE place_type_id IN (1, 2, 3, 4);

-- Step 4: 새 카테고리 삽입
-- RESTAURANT (place_type_id = 1) - 9개 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES
    (1, '한식', 'KOREAN_FOOD', CURRENT_TIMESTAMP),
    (1, '일식', 'JAPANESE_FOOD', CURRENT_TIMESTAMP),
    (1, '중식', 'CHINESE_FOOD', CURRENT_TIMESTAMP),
    (1, '양식', 'WESTERN_FOOD', CURRENT_TIMESTAMP),
    (1, '아시안', 'ASIAN_FOOD', CURRENT_TIMESTAMP),
    (1, '패스트푸드', 'FAST_FOOD', CURRENT_TIMESTAMP),
    (1, '고기', 'MEAT', CURRENT_TIMESTAMP),
    (1, '치킨', 'CHICKEN', CURRENT_TIMESTAMP),
    (1, '분식', 'SNACK_BAR', CURRENT_TIMESTAMP);

-- CAFE (place_type_id = 2) - 4개 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES
    (2, '개인카페', 'LOCAL_CAFE', CURRENT_TIMESTAMP),
    (2, '디저트', 'DESSERT_CAFE', CURRENT_TIMESTAMP),
    (2, '가맹점', 'FRANCHISE_CAFE', CURRENT_TIMESTAMP),
    (2, '보드게임', 'BOARDGAME_CAFE', CURRENT_TIMESTAMP);

-- BAR (place_type_id = 3) - 4개 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES
    (3, '요리주점', 'FOOD_BAR', CURRENT_TIMESTAMP),
    (3, '이자카야', 'IZAKAYA', CURRENT_TIMESTAMP),
    (3, '실내포차', 'INDOOR_POCHA', CURRENT_TIMESTAMP),
    (3, '칵테일바', 'COCKTAIL_BAR', CURRENT_TIMESTAMP);

-- STUDY (place_type_id = 4) - 2개 카테고리
INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES
    (4, '도서관', 'LIBRARY', CURRENT_TIMESTAMP),
    (4, '스터디카페', 'STUDY_CAFE', CURRENT_TIMESTAMP);
