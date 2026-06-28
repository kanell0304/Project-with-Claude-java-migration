# 디지털 취약계층 인터넷 뱅킹 도우미

고령화 사회에서 디지털 기기 사용이 어려운 어르신을 위한 인터넷 뱅킹 · 금융 업무 AI 안내 서비스입니다.
스마트폰(PWA) 환경에서 동작하며, 텍스트 채팅과 음성 입력을 통해 단계별 맞춤 가이드를 제공합니다.

---

## 주요 기능

- **텍스트 채팅**: 계좌이체, 잔액조회, 공과금 납부 등 금융 업무를 쉬운 말로 단계별 안내
- **음성 입력(STT)**: 마이크로 말하면 Whisper API가 텍스트로 변환 후 답변
- **음성 출력(TTS)**: AI 답변을 소리로 읽어주는 기능 (글 읽기 어려운 분 대상)
- **가이드 캐시**: 앱 + 작업 키워드를 감지해 OpenAI web_search_preview로 최신 가이드를 검색, 30일간 DB에 캐싱
- **업체 선택 UI**: 작업은 감지됐지만 앱이 특정되지 않으면 금융기관 버튼 목록 표시
- **AI 모델 표시**: 현재 연결된 OpenAI 모델명을 헤더에 실시간 표시
- **PWA 지원**: 스마트폰 홈화면 설치 가능

---

## 기술 스택

### Backend

| 구성 요소 | 기술 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.3.5 (Spring MVC) |
| 빌드 | Gradle |
| OpenAI 연동 | Spring `RestClient` (직접 REST 호출) |
| 데이터베이스 | PostgreSQL + Spring Data JPA |
| 설정 관리 | `application.properties` |
| 런타임 | 내장 Tomcat (포트 8081) |

### Frontend

| 구성 요소 | 기술 |
|---|---|
| 프레임워크 | Vite + React 19 (PWA) |
| 언어 | JavaScript |
| 스타일 | Tailwind CSS 4 |
| TTS | Web Speech API (브라우저 내장) |
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
project-with-claude-java/
├── backend/
│   ├── src/main/java/com/digitalhelper/
│   │   ├── config/
│   │   │   ├── AppConfig.java          # CORS · RestClient 빈 설정
│   │   │   └── DataInitializer.java    # 서버 시작 시 DB 시딩
│   │   ├── controller/
│   │   │   ├── HealthController.java
│   │   │   ├── ChatController.java
│   │   │   ├── VoiceController.java
│   │   │   ├── VendorController.java
│   │   │   └── TaskController.java
│   │   ├── dto/
│   │   │   ├── Message.java
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java       # model 필드 포함
│   │   │   └── VoiceResponse.java
│   │   ├── entity/
│   │   │   ├── Guide.java
│   │   │   ├── Vendor.java
│   │   │   └── Task.java
│   │   ├── repository/
│   │   │   ├── GuideRepository.java
│   │   │   ├── VendorRepository.java
│   │   │   └── TaskRepository.java
│   │   └── service/
│   │       ├── OpenAiService.java      # Chat · 웹검색 · Whisper STT
│   │       ├── GuideService.java       # 가이드 캐시 로직
│   │       └── KeywordDetector.java    # 앱·업무 키워드 감지
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── docker-compose.yml
└── frontend/
    ├── src/
    │   ├── components/
    │   │   ├── ChatWindow.jsx
    │   │   ├── MessageBubble.jsx
    │   │   ├── InputBar.jsx
    │   │   ├── VendorSelector.jsx
    │   │   └── TTSSetup.jsx            # TTS 사용 여부 선택 화면
    │   ├── hooks/
    │   │   ├── useChat.js
    │   │   ├── useVoice.js
    │   │   └── useTTS.js               # Web Speech API TTS
    │   ├── lib/
    │   │   └── api.js
    │   ├── App.jsx
    │   ├── main.jsx
    │   └── index.css
    ├── public/
    │   └── manifest.json
    ├── index.html
    ├── vite.config.js
    └── package.json
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
  "needs_app_selection": false,
  "model": "gpt-5.5"
}
```

---

## 로컬 실행

### 사전 요구사항

- Java 21+
- Gradle (IntelliJ 내장 Gradle 사용 가능)
- PostgreSQL (유저: `bankassistent`, DB: `bankassistent_db`)
- OpenAI API Key
- Node.js 18+

### 백엔드 실행

```bash
cd backend

# application.properties에 OpenAI API 키 입력
# src/main/resources/application.properties
# openai.api-key=sk-...

# IntelliJ: build.gradle 우클릭 → Gradle 프로젝트 연결 → 실행
# 또는 터미널:
./gradlew bootRun
```

서버가 `http://localhost:8081` 에서 실행됩니다.

### 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

`http://localhost:5173` 에서 실행됩니다.

### Docker로 실행

```bash
cd backend
docker compose up --build
```

---

## 주요 동작 흐름

### TTS (음성 안내) 흐름

```
앱 시작 / 새 대화 클릭
    │
    ▼
TTS 선택 화면
    ├─ [음성 안내 사용] → AI 답변 자동 읽어주기 + 헤더 음량 슬라이더 표시
    └─ [사용 안함]     → 기존 텍스트 채팅 화면
```

### 가이드 캐시 흐름

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

## 마이그레이션 이력

### Python → Java (백엔드)

초기 프로토타입은 **Python + FastAPI**로 구현되었으며, 서비스 안정성과 타입 안전성을 위해 **Spring Boot + Java**로 전면 마이그레이션했습니다.

| Python (레거시) | Java (현재) |
|---|---|
| FastAPI | Spring Boot 3.3 + Spring MVC |
| Pydantic 모델 | Java Records (DTO) |
| aiosqlite + 직접 SQL | Spring Data JPA + PostgreSQL |
| `openai` Python SDK | Spring `RestClient` |
| Maven | Gradle |

### Next.js → Vite + React (프론트엔드)

| 항목 | Before | After |
|------|--------|-------|
| 빌드 도구 | Next.js 16 | Vite 6 |
| 언어 | TypeScript | JavaScript |
| 환경변수 prefix | `NEXT_PUBLIC_` | `VITE_` |
| 빌드 출력 | `.next/` | `dist/` |

---

## 배포 (CI/CD)

`main` 브랜치에 push 되면 GitHub Actions가 자동 배포합니다.

| 변경 경로 | 배포 대상 |
|---|---|
| `backend/**` | EC2 SSH 접속 → git pull → `docker compose` 재시작 |
| `frontend/**` | Vite 빌드 → S3 업로드 → CloudFront 캐시 무효화 |

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
