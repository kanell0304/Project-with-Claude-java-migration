import json
import aiosqlite
from pathlib import Path

DB_PATH = Path(__file__).parent.parent.parent / "data" / "guides.db"

INITIAL_VENDORS = [
    # 시중은행
    ("국민은행", "금융"), ("신한은행", "금융"), ("우리은행", "금융"), ("하나은행", "금융"),
    ("농협은행", "금융"), ("기업은행", "금융"), ("SC제일은행", "금융"), ("씨티은행", "금융"),
    ("수협은행", "금융"), ("부산은행", "금융"), ("경남은행", "금융"), ("광주은행", "금융"),
    ("전북은행", "금융"), ("제주은행", "금융"), ("대구은행", "금융"), ("새마을금고", "금융"),
    ("신협", "금융"), ("우체국", "금융"),
    # 인터넷은행
    ("카카오뱅크", "금융"), ("토스뱅크", "금융"), ("케이뱅크", "금융"),
    # 핀테크
    ("토스", "금융"), ("카카오페이", "금융"), ("네이버페이", "금융"), ("페이코", "금융"),
    # 증권
    ("미래에셋증권", "금융"), ("NH투자증권", "금융"), ("삼성증권", "금융"),
    ("키움증권", "금융"), ("한국투자증권", "금융"), ("KB증권", "금융"),
]

# (name, display_name, keywords JSON)
INITIAL_TASKS = [
    ("계좌이체",    "계좌이체 / 송금",   json.dumps(["계좌이체", "송금", "이체", "보내"], ensure_ascii=False)),
    ("잔액조회",    "잔액 조회",         json.dumps(["잔액", "조회", "얼마"], ensure_ascii=False)),
    ("공과금납부",  "공과금 납부",       json.dumps(["공과금", "납부", "요금"], ensure_ascii=False)),
    ("OTP발급",    "OTP 발급",          json.dumps(["otp", "일회용비밀번호"], ensure_ascii=False)),
    ("계좌개설",    "계좌 개설",         json.dumps(["계좌개설", "계좌 만들기", "통장개설", "통장 만들기"], ensure_ascii=False)),
    ("이체한도변경", "이체 한도 변경",    json.dumps(["이체한도", "송금한도", "한도 변경", "한도변경"], ensure_ascii=False)),
]


async def init_db() -> None:
    DB_PATH.parent.mkdir(exist_ok=True)
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute("""
            CREATE TABLE IF NOT EXISTS guides (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                app_name   TEXT NOT NULL,
                task       TEXT NOT NULL,
                content    TEXT NOT NULL,
                source_url TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                expires_at DATETIME NOT NULL,
                UNIQUE(app_name, task)
            )
        """)
        try:
            await db.execute("ALTER TABLE guides ADD COLUMN source_url TEXT")
        except Exception:
            pass

        await db.execute("""
            CREATE TABLE IF NOT EXISTS vendors (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT NOT NULL UNIQUE,
                category TEXT NOT NULL
            )
        """)
        await db.executemany(
            "INSERT OR IGNORE INTO vendors (name, category) VALUES (?, ?)",
            INITIAL_VENDORS,
        )

        await db.execute("""
            CREATE TABLE IF NOT EXISTS tasks (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                name         TEXT NOT NULL UNIQUE,
                display_name TEXT NOT NULL,
                keywords     TEXT NOT NULL
            )
        """)
        await db.executemany(
            "INSERT OR IGNORE INTO tasks (name, display_name, keywords) VALUES (?, ?, ?)",
            INITIAL_TASKS,
        )

        await db.commit()


def get_db_path() -> Path:
    return DB_PATH
