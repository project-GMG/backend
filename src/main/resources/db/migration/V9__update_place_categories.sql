DELETE
FROM place_categories
WHERE place_type_id = 2;

INSERT INTO place_categories (place_type_id, name, code, created_at)
VALUES (2, '프랜차이즈', 'FRANCHISE_CAFE', CURRENT_TIMESTAMP),
       (2, '개인카페', 'LOCAL_CAFE', CURRENT_TIMESTAMP),
       (2, '디저트카페', 'DESSERT_CAFE', CURRENT_TIMESTAMP);