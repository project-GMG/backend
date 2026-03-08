CREATE TABLE feedbacks (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    rating     INT          NOT NULL,
    comment    VARCHAR(500) NULL,
    page       VARCHAR(255) NULL,
    user_agent VARCHAR(500) NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NULL,
    PRIMARY KEY (id),
    INDEX idx_feedback_created_at (created_at),
    INDEX idx_feedback_rating (rating)
);
