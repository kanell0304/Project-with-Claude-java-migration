const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export async function getTasks() {
  const res = await fetch(`${BASE_URL}/api/v1/tasks`);
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export async function sendChat(messages, sessionId) {
  const res = await fetch(`${BASE_URL}/api/v1/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ messages, session_id: sessionId }),
  });
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export async function sendVoice(audio) {
  const form = new FormData();
  form.append("audio", audio, "recording.webm");
  const res = await fetch(`${BASE_URL}/api/v1/voice`, {
    method: "POST",
    body: form,
  });
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export async function getVendors(category) {
  const url = category
    ? `${BASE_URL}/api/v1/vendors?category=${encodeURIComponent(category)}`
    : `${BASE_URL}/api/v1/vendors`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}
