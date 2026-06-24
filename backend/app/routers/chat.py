import uuid
import logging
from fastapi import APIRouter, HTTPException
from ..models.schemas import ChatRequest, ChatResponse
from ..services.openai_service import get_chat_response
from ..services.guide_service import get_or_fetch_guide, _detect_task, _detect_app

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/chat", tags=["chat"])


@router.post("", response_model=ChatResponse)
async def chat(request: ChatRequest):
    if not request.messages:
        raise HTTPException(status_code=400, detail="messages가 비어 있습니다.")

    try:
        user_messages = [m.content for m in request.messages if m.role == "user"]

        # 작업은 감지됐지만 앱은 없는 경우 → 업체 선택 요청
        task_detected = any(_detect_task(m) for m in user_messages)
        app_detected = any(_detect_app(m) for m in user_messages)
        needs_app_selection = task_detected and not app_detected

        guide_result = await get_or_fetch_guide(user_messages)
        guide_content, source_url = guide_result if guide_result else (None, None)
        reply = await get_chat_response(request.messages, guide_content)
    except Exception as e:
        logger.exception("chat 처리 중 오류 발생")
        raise HTTPException(status_code=500, detail=str(e))

    session_id = request.session_id or str(uuid.uuid4())
    return ChatResponse(
        reply=reply,
        session_id=session_id,
        guide_used=guide_content is not None,
        source_url=source_url,
        needs_app_selection=needs_app_selection,
    )
