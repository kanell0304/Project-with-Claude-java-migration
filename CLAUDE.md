# 프로젝트 개요

고령화 사회에서 디지털 취약 계층을 위한 인터넷 뱅킹, 인터넷 업무 도우미
스마트폰(PWA) 환경에서 동작하며 OpenAI API를 활용한 AI 챗봇 서비스

---

# 기술 스택

## Backend
- Python / FastAPI
- OpenAI API (GPT-4o-mini, Whisper)
- SQLite (aiosqlite) — 가이드 캐시 + 업체 목록 + 업무 목록 저장
- 가상환경: venv

## Frontend
- Next.js (PWA)
- TypeScript / Tailwind CSS

## 배포
- Git + GitHub Actions (CI/CD)
- AWS: EC2 t3.micro (백엔드 Docker + Nginx), S3 + CloudFront (프론트), Route53 (DNS)
- 프론트: Next.js 정적 빌드(output: export) → S3 → CloudFront
- 백엔드: EC2에 Docker + docker-compose + Nginx 리버스 프록시

---

# 코드 규칙

## Python
- import는 반드시 **상대 경로** 사용 (절대 경로 금지)
- 가상환경은 **venv** 사용 (miniconda 사용 안 함)

---

# 프로젝트 구조

```
Project with Claude/
├── CLAUDE.md
├── backend/
│   ├── app/
│   │   ├── core/        # 설정 (config.py)
│   │   ├── db/          # database.py, guides_repo.py
│   │   ├── models/      # Pydantic 스키마
│   │   ├── routers/     # chat, voice, vendors, tasks
│   │   ├── services/    # openai_service, guide_service
│   │   └── main.py
│   ├── data/            # guides.db (SQLite, gitignore)
│   ├── .env.example
│   ├── Dockerfile
│   └── requirements.txt
└── frontend/
    ├── app/             # Next.js App Router
    ├── components/      # ChatWindow, MessageBubble, InputBar, VendorSelector
    ├── hooks/           # useChat, useVoice
    ├── lib/             # api.ts
    └── public/          # manifest.json
```

---

# API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| GET | /health | 헬스 체크 |
| POST | /api/v1/chat | 텍스트 채팅 |
| POST | /api/v1/voice | 음성 입력 (Whisper STT + GPT) |
| GET | /api/v1/vendors | 업체 목록 조회 (?category=금융) |
| GET | /api/v1/tasks | 업무 목록 조회 |

---

# 주요 동작 흐름

## 가이드 캐시
- 사용자 메시지에서 앱(토스 등) + 작업(계좌이체 등) 키워드를 대화 이력 전체에서 감지
- DB 캐시 확인 → 없으면 OpenAI web_search_preview로 검색 → DB 저장 (30일 TTL)

## 업체 선택 UI
- 작업 키워드는 감지됐지만 앱 키워드가 없으면 `needs_app_selection: true` 반환
- 프론트에서 vendors DB의 업체 버튼 목록 표시
- 없으면 직접 입력 가능

## CI/CD 흐름
- `main` 브랜치 push 시 자동 배포
- `backend/**` 변경 → EC2 SSH 접속 → git pull → docker compose 재시작
- `frontend/**` 변경 → Next.js 빌드 → S3 업로드 → CloudFront 캐시 무효화

### GitHub Secrets 목록
| 키 | 설명 |
|----|------|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USER` | EC2 접속 유저 (보통 ec2-user 또는 ubuntu) |
| `EC2_SSH_KEY` | EC2 PEM 키 내용 |
| `OPENAI_API_KEY` | OpenAI API 키 |
| `DOMAIN` | 서비스 도메인 |
| `AWS_ACCESS_KEY_ID` | AWS IAM 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM 시크릿 키 |
| `AWS_REGION` | AWS 리전 (예: ap-northeast-2) |
| `S3_BUCKET_NAME` | 프론트 S3 버킷 이름 |
| `CLOUDFRONT_DISTRIBUTION_ID` | CloudFront 배포 ID |

## DB 시딩
- 서버 시작 시 vendors 테이블에 한국 금융기관 목록 자동 삽입 (INSERT OR IGNORE)
- 서버 시작 시 tasks 테이블에 주요 금융 업무 목록 자동 삽입 (INSERT OR IGNORE)
  - 계좌이체, 잔액조회, 공과금납부, OTP발급, 계좌개설, 이체한도변경
