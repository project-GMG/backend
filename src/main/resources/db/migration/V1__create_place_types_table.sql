CREATE TABLE place_types
(
    id    BIGINT AUTO_INCREMENT NOT NULL,
    code  VARCHAR(32) NOT NULL,
    label VARCHAR(32) NOT NULL,
    CONSTRAINT pk_place_types PRIMARY KEY (id),
    CONSTRAINT uc_place_types_code UNIQUE (code)
);

CREATE INDEX idx_code ON place_types(code);