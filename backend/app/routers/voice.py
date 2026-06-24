from fastapi import APIRouter, HTTPException, UploadFile, File
from ..models.schemas import VoiceResponse
from ..services.openai_service import transcribe_audio

router = APIRouter(prefix="/voice", tags=["voice"])


@router.post("", response_model=VoiceResponse)
async def voice_stt(audio: UploadFile = File(...)):
    allowed_types = {"audio/webm", "audio/mp4", "audio/mpeg", "audio/wav", "audio/ogg"}
    if audio.content_type not in allowed_types:
        raise HTTPException(status_code=400, detail="지원하지 않는 오디오 형식입니다.")

    try:
        audio_bytes = await audio.read()
        audio_file = (audio.filename or "audio.webm", audio_bytes, audio.content_type)
        transcript = await transcribe_audio(audio_file)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"음성 인식 오류: {str(e)}")

    return VoiceResponse(transcript=transcript)
