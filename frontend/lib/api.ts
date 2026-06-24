const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8000";

export interface Message {
  role: "user" | "assistant";
  content: string;
}

export interface ChatResponse {
  reply: string;
  session_id: string;
  guide_used: boolean;
  source_url: string | null;
  needs_app_selection: boolean;
}

export interface Vendor {
  name: string;
  category: string;
}

export interface Task {
  name: string;
  display_name: string;
  keywords: string[];
}

export async function getTasks(): Promise<Task[]> {
  const res = await fetch(`${BASE_URL}/api/v1/tasks`);
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}

export interface VoiceResponse {
  transcript: string;
}

export async function sendChat(
  messages: Message[],
  sessionId?: string
): Promise<ChatResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ messages, session_id: sessionId }),
  });

  if (!res.ok) {
    throw new Error(`서버 오류: ${res.status}`);
  }

  return res.json();
}

export async function sendVoice(audio: Blob): Promise<VoiceResponse> {
  const form = new FormData();
  form.append("audio", audio, "recording.webm");

  const res = await fetch(`${BASE_URL}/api/v1/voice`, {
    method: "POST",
    body: form,
  });

  if (!res.ok) {
    throw new Error(`서버 오류: ${res.status}`);
  }

  return res.json();
}

export async function getVendors(category?: string): Promise<Vendor[]> {
  const url = category
    ? `${BASE_URL}/api/v1/vendors?category=${encodeURIComponent(category)}`
    : `${BASE_URL}/api/v1/vendors`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
  return res.json();
}
