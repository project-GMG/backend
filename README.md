# flyway 사용법
```
1. Entity 작성
2. SQL 마이그레이션 파일 작성
3. 로컬 검증 (./gradlew flywayMigrate)
4. Git commit & push
5. Dev/Prod 배포 → 자동으로 DB 생성! 
```
# migration 파일 작성 규칙
```
src/main/resources/db/migration/
├── V1__create_place_types_table.sql
├── V2__insert_initial_place_types.sql
├── V3__create_events_table.sql
└── V4__create_event_place_types_table.sql

V{버전}__{설명}.sql
│  │      └─ 더블 언더스코어 (__) 필수!
│  └─ 버전 번호 (1, 2, 3...)
└─ 대문자 V 필수!
```

# dev 환경에서 마이그레이션이 꼬였을때
```
DROP TABLE IF EXISTS event_place_types;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS place_types;
... //기존 테이블 다 지우고
DROP TABLE IF EXISTS flyway_schema_history;
```

---

# dev에 push 할때마다 CD workflow 수동실행
- 기존에 자동으로 실행되던걸 수동 실행으로 바꿈 -> 너무 빈번한 배포를 줄이기 위해
<img width="2735" height="722" alt="image" src="https://github.com/user-attachments/assets/a88bad08-dc61-4c1e-b860-9455b2530e22" />

