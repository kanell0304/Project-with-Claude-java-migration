import logging
from ..db.guides_repo import get_guide, save_guide
from .openai_service import fetch_guide_via_search

logger = logging.getLogger(__name__)

APP_KEYWORDS: dict[str, list[str]] = {
    "토스": ["toss", "토스"],
    "카카오뱅크": ["카카오뱅크", "kakaobank"],
    "신한": ["신한", "sol", "쏠"],
    "국민": ["국민", "kb", "스타뱅킹"],
    "하나": ["하나", "하나은행"],
    "우리": ["우리", "우리은행"],
    "농협": ["농협", "nh"],
}

# 앱별 공식 헬프센터 URL
APP_HELP_URLS: dict[str, str] = {
    "토스": "https://help.toss.im",
    "카카오뱅크": "https://www.kakaobank.com/Help",
    "신한": "https://www.shinhan.com/hpe/index.jsp#050101000000",
    "국민": "https://www.kbstar.com/quics?page=C102050",
    "하나": "https://www.kebhana.com/cont/help/index.jsp",
    "우리": "https://www.wooribank.com/wb/DrawWBPHP?pgmId=PCUST0010",
    "농협": "https://www.nonghyup.com/guide/faq.do",
}

TASK_KEYWORDS: dict[str, list[str]] = {
    "계좌이체":    ["계좌이체", "송금", "이체", "보내"],
    "잔액조회":    ["잔액", "조회", "얼마"],
    "공과금납부":  ["공과금", "납부", "요금"],
    "OTP발급":    ["otp", "일회용비밀번호"],
    "계좌개설":    ["계좌개설", "계좌 만들기", "통장개설", "통장 만들기"],
    "이체한도변경": ["이체한도", "송금한도", "한도 변경", "한도변경"],
}


def _detect_app(text: str) -> str | None:
    text_lower = text.lower()
    for app_name, keywords in APP_KEYWORDS.items():
        if any(k in text_lower for k in keywords):
            return app_name
    return None


def _detect_task(text: str) -> str | None:
    text_lower = text.lower()
    for task_name, keywords in TASK_KEYWORDS.items():
        if any(k in text_lower for k in keywords):
            return task_name
    return None


def detect_from_history(messages: list[str]) -> tuple[str, str] | None:
    """대화 이력 전체에서 앱과 작업을 각각 수집해 조합"""
    detected_app = None
    detected_task = None

    # 최신 메시지 우선으로 탐색
    for msg in reversed(messages):
        if not detected_app:
            detected_app = _detect_app(msg)
        if not detected_task:
            detected_task = _detect_task(msg)
        if detected_app and detected_task:
            break

    if detected_app and detected_task:
        return detected_app, detected_task
    return None


async def get_or_fetch_guide(messages: list[str]) -> tuple[str, str | None] | None:
    detected = detect_from_history(messages)
    if not detected:
        logger.info("[가이드] 앱/작업 키워드 감지 안됨 → 일반 응답")
        return None

    app_name, task = detected
    logger.info("[가이드] 감지: 앱=%s, 작업=%s", app_name, task)

    cached = await get_guide(app_name, task)
    if cached:
        content, source_url = cached
        logger.info("[가이드] DB 캐시 히트 → web search 생략 (출처: %s)", source_url)
        return content, source_url

    logger.info("[가이드] 캐시 없음 → web search 시작")
    content, _ = await fetch_guide_via_search(app_name, task)
    source_url = APP_HELP_URLS.get(app_name)
    await save_guide(app_name, task, content, source_url)
    logger.info("[가이드] web search 완료 → DB 저장 (출처: %s)", source_url)
    return content, source_url
