-- Events 테이블 생성
CREATE TABLE events (
                        id BIGINT AUTO_INCREMENT NOT NULL,
                        hash_url VARCHAR(64) NOT NULL,
                        title VARCHAR(50) NOT NULL,
                        center_latitude DECIMAL(10, 7) NOT NULL,
                        center_longitude DECIMAL(10, 7) NOT NULL,
                        location_name VARCHAR(255),
                        date_start DATE NOT NULL,
                        date_end DATE NOT NULL,
                        time_start TIME NOT NULL,
                        time_end TIME NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                        timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Seoul',
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT pk_events PRIMARY KEY (id),
                        CONSTRAINT uc_events_hash_url UNIQUE (hash_url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_hash_url ON events(hash_url);
CREATE INDEX idx_status ON events(status);
CREATE INDEX idx_created_at ON events(created_at);