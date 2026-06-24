import logging
from openai import AsyncOpenAI
from ..core.config import settings
from ..models.schemas import Message

logger = logging.getLogger(__name__)

client = AsyncOpenAI(api_key=settings.openai_api_key)

BASE_SYSTEM_PROMPT = """당신은 디지털 기기 사용이 어려운 어르신들을 위한 친절한 도우미입니다.

역할:
- 인터넷 뱅킹(계좌이체, 공과금 납부, OTP 발급 등) 사용 방법을 단계별로 안내합니다.
- 금융 서류(신청서, 확인서 등) 작성 방법을 쉽게 설명합니다.

규칙:
- 어려운 용어는 반드시 쉬운 말로 풀어 설명합니다.
- 한 번에 하나의 단계만 안내합니다.
- 사용자가 "다음", "했어요", "눌렀어요" 등이라고 하면 다음 단계를 안내합니다.
- 금융 정보의 정확성이 중요한 경우, 직접 은행에 문의하도록 안내합니다.
- 주민등록번호 등 민감한 개인정보는 절대 입력하지 말라고 안내합니다.
- 항상 친절하고 천천히, 이해하기 쉬운 말로 답변합니다.
- 답변은 짧고 명확하게 작성합니다 (한 번에 너무 많은 정보를 주지 않습니다).
"""


def build_system_prompt(guide: str | None = None) -> str:
    if not guide:
        return BASE_SYSTEM_PROMPT
    return BASE_SYSTEM_PROMPT + f"\n\n[참고 가이드]\n{guide}\n위 가이드를 반드시 참고하여 정확하게 안내하세요."


async def get_chat_response(messages: list[Message], guide: str | None = None) -> str:
    formatted = [{"role": "system", "content": build_system_prompt(guide)}]
    formatted += [{"role": m.role, "content": m.content} for m in messages]

    response = await client.chat.completions.create(
        model=settings.openai_model,
        messages=formatted,
        max_tokens=500,
        temperature=0.7,
    )
    return response.choices[0].message.content


async def fetch_guide_via_search(app_name: str, task: str) -> tuple[str, str | None]:
    query = f"{app_name} 앱 {task} 방법 단계별 안내"
    response = await client.responses.create(
        model="gpt-4o-mini",
        tools=[{"type": "web_search_preview"}],
        input=query,
    )

    return response.output_text, None


async def transcribe_audio(audio_file) -> str:
    transcript = await client.audio.transcriptions.create(
        model="whisper-1",
        file=audio_file,
        language="ko",
    )
    return transcript.text
