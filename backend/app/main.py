from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .core.config import settings
from .routers import chat, voice, vendors, tasks
from .db.database import init_db


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    yield

app = FastAPI(
    title="디지털 취약계층 도우미 API",
    description="고령자를 위한 인터넷 뱅킹 및 서류 작성 안내 서비스",
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat.router, prefix="/api/v1")
app.include_router(voice.router, prefix="/api/v1")
app.include_router(vendors.router, prefix="/api/v1")
app.include_router(tasks.router, prefix="/api/v1")


@app.get("/health")
async def health_check():
    return {"status": "ok"}
