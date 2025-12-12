-- Places 테이블 생성
CREATE TABLE places
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)     NOT NULL,
    place_type_id   BIGINT           NOT NULL,
    category_id     BIGINT           NULL,
    latitude        DECIMAL(10, 7)   NOT NULL,
    longitude       DECIMAL(10, 7)   NOT NULL,
    address         VARCHAR(255)     NULL,
    image_url       VARCHAR(500)     NULL,
    rating          DECIMAL(2, 1)    NOT NULL DEFAULT 0.0,
    open_hours_json JSON             NULL,
    is_active       BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_places PRIMARY KEY (id)
);

-- 외래 키 제약조건 추가
ALTER TABLE places
    ADD CONSTRAINT fk_places_place_type
        FOREIGN KEY (place_type_id) REFERENCES place_types (id) ON DELETE RESTRICT;

ALTER TABLE places
    ADD CONSTRAINT fk_places_category
        FOREIGN KEY (category_id) REFERENCES place_categories (id) ON DELETE SET NULL;

-- 인덱스 생성
CREATE INDEX idx_location ON places (latitude, longitude);
CREATE INDEX idx_is_active ON places (is_active);