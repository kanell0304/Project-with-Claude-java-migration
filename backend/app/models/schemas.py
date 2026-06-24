from pydantic import BaseModel
from typing import Literal


class Message(BaseModel):
    role: Literal["user", "assistant", "system"]
    content: str


class ChatRequest(BaseModel):
    messages: list[Message]
    session_id: str | None = None


class ChatResponse(BaseModel):
    reply: str
    session_id: str
    guide_used: bool = False
    source_url: str | None = None
    needs_app_selection: bool = False


class VoiceResponse(BaseModel):
    transcript: str
