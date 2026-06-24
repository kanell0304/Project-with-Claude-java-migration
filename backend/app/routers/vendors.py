import aiosqlite
from fastapi import APIRouter
from ..db.database import get_db_path

router = APIRouter(prefix="/vendors", tags=["vendors"])


@router.get("")
async def get_vendors(category: str | None = None):
    async with aiosqlite.connect(get_db_path()) as db:
        if category:
            async with db.execute(
                "SELECT name, category FROM vendors WHERE category = ? ORDER BY name",
                (category,),
            ) as cursor:
                rows = await cursor.fetchall()
        else:
            async with db.execute(
                "SELECT name, category FROM vendors ORDER BY category, name"
            ) as cursor:
                rows = await cursor.fetchall()

    return [{"name": r[0], "category": r[1]} for r in rows]
