import json
import aiosqlite
from fastapi import APIRouter
from ..db.database import get_db_path

router = APIRouter(prefix="/tasks", tags=["tasks"])


@router.get("")
async def get_tasks():
    async with aiosqlite.connect(get_db_path()) as db:
        async with db.execute(
            "SELECT name, display_name, keywords FROM tasks ORDER BY id"
        ) as cursor:
            rows = await cursor.fetchall()

    return [
        {
            "name": r[0],
            "display_name": r[1],
            "keywords": json.loads(r[2]),
        }
        for r in rows
    ]
