# Auto Accounting Processor (Backend)

은행 거래 내역(CSV)을 자동으로 분석·분류하여 DB에 저장하고, 사업체별로 조회하는 Java Spring Boot 백엔드입니다.

### [🔎 프론트 레포지토리 링크 accounting-dashboard](http://github.com/rorrxr/accounting-dashboard)

## 🎯 프로젝트 개요
하나의 계좌에서 발생한 거래 내역을 A 커머스(com_1), B 커머스(com_2) 등 여러 사업체 기준으로 자동 분류합니다.

규칙 파일(`rules.json`)의 키워드 기반으로 거래 설명(적요)을 매칭해 회사/계정과목을 결정합니다.

## 🚀 주요 기능

### 기능 1: 자동 회계 처리 API
- **Endpoint**: `POST /api/v1/accounting/process`
- **입력**: `bank_transactions.csv`, `rules.json` (multipart/form-data)
- **동작**: CSV를 파싱하여 규칙에 따라 각 거래를 회사/카테고리로 분류 후 DB 저장
- **출력**: 처리 통계(전체/분류/미분류 수), 회사별 요약 등

### 기능 2: 사업체별 분류 결과 조회 API
- **Endpoint**: `GET /api/v1/accounting/records?companyId={companyId}&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- **동작**: 특정 회사에 귀속된 거래 목록 + 분류된 카테고리 정보 반환
- **미분류 처리**: 규칙과 매칭되지 않은 거래는 `classification_status = 'UNCLASSIFIED'` 로 저장/조회

## 🛠️ 기술 스택 (현재 빌드 기준)
- **Language**: Java 21 (Gradle Toolchain)
- **Framework**: Spring Boot 3.2.4
- **Build**: Gradle
- **ORM**: Spring Data JPA (Hibernate)
- **DB 드라이버**: MySQL (prod), H2 (local test)
- **API 문서**: springdoc-openapi (Swagger UI)
- **CSV 파싱**: Apache Commons CSV
- **Lombok / Devtools**: 개발 편의

> 🔎 **주의**
> - 현재 의존성에는 Spring Security/JWT, Redis, Actuator가 없습니다.
> - 따라서 인증/인가, 헬스체크 엔드포인트(`/actuator/health`), 캐시 사용은 README에서 제외/보류 처리합니다.
> - Jackson은 `spring-boot-starter-web`에 포함되지만, 프로젝트에서 명시적 의존성(`jackson-core/annotations/databind`)을 사용 중입니다.

## ✨ 기술적 의사 결정
- **Java 21 + Spring Boot 3.2**: 최신 LTS 런타임/프레임워크로 Record/Virtual Thread 등의 현대적 기능과 생태계 활용.
- **JPA(Hibernate)**: 빠른 개발, 안정적 트랜잭션 처리, 스키마 자동화로 초기 생산성↑.
- **MySQL 드라이버 + H2**: 로컬은 H2로 가볍게, 배포는 MySQL로 운영 친화.
- **Commons CSV**: 대용량 CSV 스트리밍 파싱에 검증된 라이브러리.
- **springdoc-openapi**: Swagger UI로 API 탐색/테스트 용이.

> 차기 단계에서 Redis/Actuator/Security(JWT)를 추가하면, 캐시 최적화/모니터링/보안을 손쉽게 끌어올릴 수 있습니다.

## 🔧 트러블슈팅
- CSV 인코딩/헤더 미스매치 시 파싱 실패 → 업로드 파일의 **UTF-8**, 헤더명 점검.

## 🏗️ 시스템 아키텍처 개요
```
[File Upload (CSV/JSON)] → [Spring Boot Application] → [MySQL (or H2)]
                             │
                             └─ [Classification Service (키워드 규칙 매칭)]
```
규칙은 메모리로 로드하여 매칭(정확 일치 → 부분 일치 점수화 → 최고 점수 선택)의 순서로 분류합니다.

## 📊 데이터베이스 스키마 (MySQL 기준)
(스키마 SQL 내용 동일)

## 🔍 핵심 자동 분류 로직
1. **완전 일치 우선**
2. **부분 일치 점수화** (예: 포함 키워드 개수/길이/우선순위 기반)
3. **최고 점수 규칙 선택** (임계값 미만이면 `UNCLASSIFIED`)

> 확장 아이디어(차기): 제외 키워드, 금액/시간대 조건, 우선순위(priority), ML 보조 분류(TF‑IDF + 로지스틱 회귀) 등 규칙 엔진 고도화.

---

## ⚙️ 개발/배포 실행 방법

### 1️⃣ Git Clone
```bash
git clone https://github.com/rorrxr/auto-accounting-processor.git
cd auto-accounting-processor
```

### 2️⃣ .env 파일 생성
```env
# MySQL
MYSQL_ROOT_PASSWORD={데이터베이스 root 비밀번호}
MYSQL_DATABASE={데이터베이스 이름}
DB_USERNAME={DB 계정명}
DB_PASSWORD={DB 계정 비밀번호}

# Spring Datasource URL
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/{데이터베이스 이름}?useSSL=false&allowPublicKeyRetrieval=true
```

### 3️⃣ Docker 이미지 빌드
```bash
docker buildx build --platform linux/amd64   -f auto-accounting-processor/Dockerfile   -t auto-accounting-processor:latest . --load
```

### 4️⃣ Docker Compose 실행
```bash
docker-compose up --build -d
```

`docker-compose.yml` 예시:
```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-hmysql-db", "-uroot", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    ports:
      - "3307:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - commerce-network

  app:
    build:
      context: .
      dockerfile: Dockerfile
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/${MYSQL_DATABASE}
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: create-drop
    ports:
      - "8080:8080"
    networks:
      - commerce-network

volumes:
  mysql-data:

networks:
  commerce-network:
    driver: bridge
```

### 5️⃣ API 확인
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- 헬스체크(Actuator 추가 시): `http://localhost:8080/actuator/health`

---

## 🔐 보안 & 운영
- 현재는 무인증 구조
- 실서비스 전 Spring Security + JWT 적용 권장