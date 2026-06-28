const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

function authHeaders(token, extra = {}) {
  return { Authorization: `Bearer ${token}`, ...extra };
}

export async function getTasks(token) {
  const res = await fetch(`${BASE_URL}/api/v1/tasks`, {
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export async function sendChat(messages, sessionId, token) {
  const res = await fetch(`${BASE_URL}/api/v1/chat`, {
    method: "POST",
    headers: authHeaders(token, { "Content-Type": "application/json" }),
    body: JSON.stringify({ messages, session_id: sessionId }),
  });
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export async function sendVoice(audio, token) {
  const form = new FormData();
  form.append("audio", audio, "recording.webm");
  const res = await fetch(`${BASE_URL}/api/v1/voice`, {
    method: "POST",
    headers: authHeaders(token),
    body: form,
  });
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export async function getVendors(category, token) {
  const url = category
    ? `${BASE_URL}/api/v1/vendors?category=${encodeURIComponent(category)}`
    : `${BASE_URL}/api/v1/vendors`;
  const res = await fetch(url, { headers: authHeaders(token) });
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}
