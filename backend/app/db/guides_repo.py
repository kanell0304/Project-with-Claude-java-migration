import aiosqlite
from datetime import datetime, timedelta
from .database import get_db_path

CACHE_DAYS = 30


async def get_guide(app_name: str, task: str) -> tuple[str, str | None] | None:
    async with aiosqlite.connect(get_db_path()) as db:
        async with db.execute(
            """
            SELECT content, source_url FROM guides
            WHERE app_name = ? AND task = ? AND expires_at > CURRENT_TIMESTAMP
            """,
            (app_name, task),
        ) as cursor:
            row = await cursor.fetchone()
            return (row[0], row[1]) if row else None


async def save_guide(app_name: str, task: str, content: str, source_url: str | None = None) -> None:
    expires_at = datetime.utcnow() + timedelta(days=CACHE_DAYS)
    async with aiosqlite.connect(get_db_path()) as db:
        await db.execute(
            """
            INSERT INTO guides (app_name, task, content, source_url, expires_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(app_name, task) DO UPDATE SET
                content    = excluded.content,
                source_url = excluded.source_url,
                created_at = CURRENT_TIMESTAMP,
                expires_at = excluded.expires_at
            """,
            (app_name, task, content, source_url, expires_at.isoformat()),
        )
        await db.commit()
