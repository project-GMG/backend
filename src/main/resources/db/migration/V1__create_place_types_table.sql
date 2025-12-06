CREATE TABLE place_types
(
    id    BIGINT AUTO_INCREMENT NOT NULL,
    code  VARCHAR(32) NOT NULL,
    label VARCHAR(32) NOT NULL,
    CONSTRAINT pk_place_types PRIMARY KEY (id),
    CONSTRAINT uc_place_types_code UNIQUE (code),
    INDEX idx_code (code)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;