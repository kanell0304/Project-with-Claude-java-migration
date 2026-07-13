# 프로젝트 개요

고령화 사회에서 디지털 취약 계층을 위한 인터넷 뱅킹, 인터넷 업무 도우미
스마트폰(PWA) 환경에서 동작하며 OpenAI API를 활용한 AI 챗봇 서비스

---

# 기술 스택

## Backend
- Java 21 / Spring Boot 3.3.5
- Gradle
- Spring Data JPA + Hibernate
- PostgreSQL — 가이드 캐시 + 업체 목록 + 업무 목록 + 유저 저장
- OpenAI API (gpt-5.5, Whisper)
- Spring Security + Kakao OAuth2 Client
- JWT 인증 (jjwt 0.12.6) — 30일 만료
- Bucket4j — IP/계정 기반 Rate Limiting (시간당 30회, 일일 100회)
- 서버 포트: 8081

## Frontend
- Vite + React 19 (PWA)
- JavaScript / Tailwind CSS 4
- Web Speech API (TTS)
- 카카오 OAuth2 로그인 (헤더 로그인/로그아웃 버튼)
- JWT localStorage 저장 (`dh_token` 키)

## 배포
- Git + GitHub Actions (CI/CD)
- AWS: EC2 t3.micro (백엔드 Docker + Nginx), S3 + CloudFront (프론트), Route53 (DNS)
- 프론트: Vite 정적 빌드(dist/) → S3 → CloudFront
- 백엔드: EC2에 Docker + docker-compose + Nginx 리버스 프록시

---

# 코드 규칙

## Java
- 패키지: `com.digitalhelper`
- 빌드 도구: Gradle (`./gradlew`)

## JavaScript (Frontend)
- 환경변수 prefix: `VITE_` (예: `VITE_API_URL`)
- 컴포넌트 파일 확장자: `.jsx`, 훅/유틸: `.js`

---

# 프로젝트 구조

```
project-with-claude-java/
├── CLAUDE.md
├── backend/
│   ├── src/main/java/com/digitalhelper/
│   │   ├── config/      # AppConfig, DataInitializer, SecurityConfig,
│   │   │                # OAuth2SuccessHandler, JwtAuthFilter, RateLimitFilter
│   │   ├── controller/  # Chat, Voice, Vendor, Task, Health
│   │   ├── dto/         # ChatRequest, ChatResponse (model 필드 포함), Message, VoiceResponse
│   │   ├── entity/      # Guide, Task, Vendor, User
│   │   ├── repository/  # JPA Repositories (+ UserRepository)
│   │   ├── service/     # OpenAiService, GuideService, KeywordDetector, JwtService
│   │   └── DigitalHelperApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── build.gradle
│   └── settings.gradle
└── frontend/
    ├── src/
    │   ├── components/  # ChatWindow, MessageBubble, InputBar, VendorSelector, TTSSetup, LoginScreen
    │   ├── hooks/       # useAuth, useChat, useVoice, useTTS
    │   ├── lib/         # api.js (JWT Authorization 헤더 포함)
    │   ├── App.jsx
    │   ├── main.jsx
    │   └── index.css
    ├── public/          # manifest.json
    ├── index.html
    ├── vite.config.js
    └── package.json
```

---

# API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| GET | /health | 헬스 체크 |
| GET | /oauth2/authorization/kakao | 카카오 로그인 시작 |
| GET | /login/oauth2/code/kakao | 카카오 OAuth2 콜백 |
| POST | /api/v1/chat | 텍스트 채팅 (응답에 model 필드 포함) |
| POST | /api/v1/voice | 음성 입력 (Whisper STT + GPT) |
| GET | /api/v1/vendors | 업체 목록 조회 (?category=금융) |
| GET | /api/v1/tasks | 업무 목록 조회 |

> 로그인 후 API 요청 시 `Authorization: Bearer {JWT}` 헤더 포함
> 비로그인도 이용 가능 (IP 기반 Rate Limit 적용)

---

# 주요 동작 흐름

## 카카오 로그인
- 헤더 로그인 버튼 → `/oauth2/authorization/kakao` 이동
- 카카오 인증 완료 → `/login/oauth2/code/kakao` 콜백
- OAuth2SuccessHandler: kakaoId로 User 생성/업데이트 → JWT 발급
- 프론트엔드 `/auth/callback?token={jwt}` 로 리다이렉트
- useAuth.js에서 토큰 파싱 → localStorage 저장
- 로그아웃: localStorage 토큰 삭제 (카카오 공식 로그아웃 미사용)

## TTS (음성 안내)
- 앱 시작 또는 새 대화 시 TTS 사용 여부 선택 화면 표시
- 사용 선택 시 AI 답변을 Web Speech API로 자동 읽어줌 (한국어, 속도 0.9)
- 헤더에 음량 슬라이더 표시 (다음 텍스트부터 적용)

## AI 모델 표시
- 채팅 응답의 `model` 필드를 헤더 타이틀 아래 뱃지로 실시간 표시
- `application.properties`의 `openai.model` 값이 자동 반영됨

## 가이드 캐시
- 사용자 메시지에서 앱(토스 등) + 작업(계좌이체 등) 키워드를 대화 이력 전체에서 감지
- DB 캐시 확인 → 없으면 OpenAI web_search_preview로 검색 → DB 저장 (30일 TTL)

## 업체 선택 UI
- 작업 키워드는 감지됐지만 앱 키워드가 없으면 `needs_app_selection: true` 반환
- 프론트에서 vendors DB의 업체 버튼 목록 표시
- 없으면 직접 입력 가능

## Rate Limiting
- 비로그인: IP 기반 (`ip:{ip}`) — 시간당 30회, 일일 100회
- 로그인: 계정 기반 (`user:{kakaoId}`) — 동일 한도
- 한도 초과 시 OpenAI 미호출, 즉시 429 반환

## CI/CD 흐름
- `main` 브랜치 push 시 자동 배포
- `backend/**` 변경 → EC2 SSH 접속 → git pull → docker compose 재시작
- `frontend/**` 변경 → Vite 빌드 → S3 업로드 → CloudFront 캐시 무효화

### GitHub Secrets 목록
| 키 | 설명 |
|----|------|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USER` | EC2 접속 유저 (보통 ec2-user 또는 ubuntu) |
| `EC2_SSH_KEY` | EC2 PEM 키 내용 |
| `OPENAI_API_KEY` | OpenAI API 키 |
| `DB_USERNAME` | PostgreSQL 사용자명 |
| `DB_PASSWORD` | PostgreSQL 비밀번호 |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret |
| `JWT_SECRET` | JWT 서명 시크릿 (32바이트 Base64) |
| `DOMAIN` | 서비스 도메인 |
| `AWS_ACCESS_KEY_ID` | AWS IAM 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM 시크릿 키 |
| `AWS_REGION` | AWS 리전 (예: ap-northeast-2) |
| `S3_BUCKET_NAME` | 프론트 S3 버킷 이름 |
| `CLOUDFRONT_DISTRIBUTION_ID` | CloudFront 배포 ID |

## DB 시딩
- 서버 시작 시 vendors 테이블에 한국 금융기관 목록 자동 삽입 (on conflict 무시)
- 서버 시작 시 tasks 테이블에 주요 금융 업무 목록 자동 삽입 (on conflict 무시)
  - 계좌이체, 잔액조회, 공과금납부, OTP발급, 계좌개설, 이체한도변경
- users 테이블은 Hibernate ddl-auto=update로 자동 생성
