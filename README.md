# 디지털 취약계층 인터넷 뱅킹 도우미

고령화 사회에서 디지털 기기 사용이 어려운 어르신을 위한 인터넷 뱅킹 · 금융 업무 AI 안내 서비스입니다.
스마트폰(PWA) 환경에서 동작하며, 텍스트 채팅과 음성 입력을 통해 단계별 맞춤 가이드를 제공합니다.

---

## 주요 기능

- **텍스트 채팅**: 계좌이체, 잔액조회, 공과금 납부 등 금융 업무를 쉬운 말로 단계별 안내
- **음성 입력(STT)**: 마이크로 말하면 Whisper API가 텍스트로 변환 후 답변
- **가이드 캐시**: 앱 + 작업 키워드를 감지해 OpenAI web_search_preview로 최신 가이드를 검색, 30일간 DB에 캐싱
- **업체 선택 UI**: 작업은 감지됐지만 앱이 특정되지 않으면 금융기관 버튼 목록 표시
- **PWA 지원**: 스마트폰 홈화면 설치 가능

---

## 기술 스택

### Backend (Java — 현재)

| 구성 요소 | 기술 |
|---|---|
| 프레임워크 | Spring Boot 3.3 (Spring MVC) |
| OpenAI 연동 | Spring `RestClient` (직접 REST 호출) |
| 데이터베이스 | SQLite + Spring Data JPA + Hibernate Community Dialect |
| 설정 관리 | `application.properties` + 환경변수 |
| 빌드 | Maven |
| 런타임 | Java 21 (내장 Tomcat) |

### Frontend

| 구성 요소 | 기술 |
|---|---|
| 프레임워크 | Next.js (App Router, PWA) |
| 언어 | TypeScript |
| 스타일 | Tailwind CSS |
| 배포 | S3 + CloudFront (정적 빌드) |

### 인프라

| 구성 요소 | 기술 |
|---|---|
| 백엔드 서버 | AWS EC2 t3.micro + Docker + Nginx |
| 프론트 CDN | AWS S3 + CloudFront |
| DNS | AWS Route53 |
| CI/CD | GitHub Actions |

---

## 프로젝트 구조

```
Project-with-Claude/
├── backend-java/                  # Java 백엔드 (현재)
│   ├── src/main/java/com/digitalhelper/
│   │   ├── config/
│   │   │   ├── AppConfig.java     # CORS · RestClient 빈 설정
│   │   │   └── DataInitializer.java  # 서버 시작 시 DB 시딩
│   │   ├── controller/            # REST API 엔드포인트
│   │   │   ├── HealthController.java
│   │   │   ├── ChatController.java
│   │   │   ├── VoiceController.java
│   │   │   ├── VendorController.java
│   │   │   └── TaskController.java
│   │   ├── dto/                   # 요청/응답 Java Records
│   │   │   ├── Message.java
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java
│   │   │   └── VoiceResponse.java
│   │   ├── entity/                # JPA 엔티티
│   │   │   ├── Guide.java
│   │   │   ├── Vendor.java
│   │   │   └── Task.java
│   │   ├── repository/            # Spring Data JPA 레포지토리
│   │   │   ├── GuideRepository.java
│   │   │   ├── VendorRepository.java
│   │   │   └── TaskRepository.java
│   │   └── service/
│   │       ├── OpenAiService.java  # Chat · 웹검색 · Whisper STT
│   │       ├── GuideService.java   # 가이드 캐시 로직
│   │       └── KeywordDetector.java # 앱·업무 키워드 감지
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── pom.xml
├── backend/                       # Python 백엔드 (레거시)
├── frontend/                      # Next.js 프론트엔드
│   ├── app/
│   ├── components/                # ChatWindow, MessageBubble, InputBar, VendorSelector
│   ├── hooks/                     # useChat, useVoice
│   └── lib/                       # api.ts
└── README.md
```

---

## API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/health` | 헬스 체크 |
| POST | `/api/v1/chat` | 텍스트 채팅 |
| POST | `/api/v1/voice` | 음성 입력 (Whisper STT) |
| GET | `/api/v1/vendors` | 금융기관 목록 (`?category=금융`) |
| GET | `/api/v1/tasks` | 업무 목록 |

### 채팅 요청/응답 예시

```json
// POST /api/v1/chat
{
  "messages": [
    { "role": "user", "content": "토스에서 계좌이체 어떻게 해요?" }
  ],
  "session_id": null
}

// 응답
{
  "reply": "토스 앱에서 계좌이체하는 방법을 알려드릴게요 ...",
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "guide_used": true,
  "source_url": "https://help.toss.im",
  "needs_app_selection": false
}
```

---

## 로컬 실행

### 사전 요구사항

- Java 21+
- Maven 3.9+
- OpenAI API Key

### 백엔드 실행

```bash
cd backend-java

# 환경변수 파일 생성
cp .env.example .env
# .env 파일에 OPENAI_API_KEY 입력

# 로컬 실행
mvn spring-boot:run

# 빌드 후 JAR 실행
mvn package -DskipTests
java -jar target/digital-helper-0.1.0.jar
```

### Docker로 실행

```bash
cd backend-java
cp .env.example .env   # OPENAI_API_KEY 입력

docker compose up --build
```

서버가 `http://localhost:8000` 에서 실행됩니다.

---

## 주요 동작 흐름

### 가이드 캐시

```
사용자 메시지
    │
    ▼
키워드 감지 (앱: 토스/카카오뱅크/신한 등, 업무: 계좌이체/잔액조회 등)
    │
    ├─ 감지 실패 → GPT 일반 응답
    │
    └─ 감지 성공
           │
           ▼
       DB 캐시 확인 (30일 TTL)
           │
           ├─ 캐시 HIT  → 캐시된 가이드 사용
           │
           └─ 캐시 MISS → OpenAI web_search_preview 검색 → DB 저장 → 가이드 사용
```

### 업체 선택 UI

업무 키워드(예: "계좌이체")는 감지됐지만 앱 키워드가 없으면 `needs_app_selection: true`를 반환합니다.  
프론트엔드는 이 값을 보고 금융기관 버튼 목록을 표시하며, 사용자가 선택하거나 직접 입력할 수 있습니다.

---

## Python → Java 마이그레이션

### 배경

초기 프로토타입은 **Python + FastAPI**로 빠르게 구현되었습니다.  
서비스 안정성, 타입 안전성, 엔터프라이즈 금융 도메인 적합성을 높이기 위해 **Spring Boot 기반 Java**로 전면 마이그레이션했습니다.

### 기술 대응 표

| Python (레거시) | Java (현재) | 비고 |
|---|---|---|
| FastAPI | Spring Boot 3.3 + Spring MVC | 동기 방식, 단순한 구조 유지 |
| Pydantic 모델 | Java Records (DTO) | 불변 데이터 클래스 |
| aiosqlite + 직접 SQL | Spring Data JPA + SQLite Dialect | JPA 쿼리 메서드·JPQL 사용 |
| `openai` Python SDK | Spring `RestClient` (직접 REST 호출) | web_search_preview 포함 전체 API 지원 |
| `python-dotenv` | `application.properties` + 환경변수 | Spring 표준 설정 방식 |
| lifespan DB 초기화 | `CommandLineRunner` (DataInitializer) | 서버 시작 시 vendors·tasks 시딩 |
| uvicorn | 내장 Tomcat | Dockerfile만 교체, Nginx 재사용 |
| async/await | 동기 스레드풀 (MVC) | 트래픽 규모상 충분 |

### 유지된 것

- **API 엔드포인트 경로 및 JSON 스키마** — 프론트엔드 변경 없음
- **SQLite 파일 경로** (`./data/guides.db`) — 기존 캐시 데이터 그대로 이전 가능
- **Dockerfile 포트** (`8000`), **Nginx 설정**, **docker-compose 구조** — 인프라 변경 없음
- **GitHub Actions CI/CD** — `backend/**` 경로만 `backend-java/**`로 수정

---

## 배포 (CI/CD)

`main` 브랜치에 push 되면 GitHub Actions가 자동 배포합니다.

| 변경 경로 | 배포 대상 |
|---|---|
| `backend-java/**` | EC2 SSH 접속 → git pull → `docker compose` 재시작 |
| `frontend/**` | Next.js 빌드 → S3 업로드 → CloudFront 캐시 무효화 |

### 필요한 GitHub Secrets

| 키 | 설명 |
|---|---|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USER` | EC2 접속 유저 |
| `EC2_SSH_KEY` | EC2 PEM 키 내용 |
| `OPENAI_API_KEY` | OpenAI API 키 |
| `DOMAIN` | 서비스 도메인 |
| `AWS_ACCESS_KEY_ID` | AWS IAM 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM 시크릿 키 |
| `AWS_REGION` | AWS 리전 (예: `ap-northeast-2`) |
| `S3_BUCKET_NAME` | 프론트 S3 버킷 이름 |
| `CLOUDFRONT_DISTRIBUTION_ID` | CloudFront 배포 ID |
