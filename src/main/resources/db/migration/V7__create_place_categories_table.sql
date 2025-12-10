CREATE TABLE place_categories
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    place_type_id BIGINT      NOT NULL,
    name          VARCHAR(50) NOT NULL,
    code          VARCHAR(50) NULL,
    created_at    TIMESTAMP   NOT NULL,

    CONSTRAINT pk_place_categories PRIMARY KEY (id),
    CONSTRAINT uc_place_categories_code UNIQUE (code)
);

ALTER TABLE place_categories
    ADD CONSTRAINT fk_place_categories_place_type
        FOREIGN KEY (place_type_id) REFERENCES place_types (id) ON DELETE RESTRICT;