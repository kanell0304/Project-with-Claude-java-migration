"use client";

import { useState, useCallback } from "react";
import { sendChat, Message } from "@/lib/api";

export interface AssistantMeta {
  guide_used: boolean;
  source_url: string | null;
}

export interface ChatMessage extends Message {
  meta?: AssistantMeta;
}

export function useChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessionId, setSessionId] = useState<string | undefined>();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [needsAppSelection, setNeedsAppSelection] = useState(false);

  const sendMessage = useCallback(
    async (content: string) => {
      const userMessage: ChatMessage = { role: "user", content };
      const next = [...messages, userMessage];
      setMessages(next);
      setLoading(true);
      setError(null);
      setNeedsAppSelection(false);

      try {
        const res = await sendChat(next, sessionId);
        setSessionId(res.session_id);
        setNeedsAppSelection(res.needs_app_selection);
        setMessages([
          ...next,
          {
            role: "assistant",
            content: res.reply,
            meta: { guide_used: res.guide_used, source_url: res.source_url },
          },
        ]);
      } catch (e) {
        setError((e as Error).message);
      } finally {
        setLoading(false);
      }
    },
    [messages, sessionId]
  );

  const reset = useCallback(() => {
    setMessages([]);
    setSessionId(undefined);
    setError(null);
    setNeedsAppSelection(false);
  }, []);

  return { messages, loading, error, needsAppSelection, sendMessage, reset };
}
