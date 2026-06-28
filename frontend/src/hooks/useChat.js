import { useState, useCallback } from "react";
import { sendChat } from "../lib/api";

export function useChat() {
  const [messages, setMessages] = useState([]);
  const [sessionId, setSessionId] = useState(undefined);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [needsAppSelection, setNeedsAppSelection] = useState(false);
  const [model, setModel] = useState(null);

  const sendMessage = useCallback(
    async (content) => {
      const userMessage = { role: "user", content };
      const next = [...messages, userMessage];
      setMessages(next);
      setLoading(true);
      setError(null);
      setNeedsAppSelection(false);

      try {
        const res = await sendChat(next, sessionId);
        setSessionId(res.session_id);
        setNeedsAppSelection(res.needs_app_selection);
        if (res.model) setModel(res.model);
        setMessages([
          ...next,
          {
            role: "assistant",
            content: res.reply,
            meta: { guide_used: res.guide_used, source_url: res.source_url },
          },
        ]);
      } catch (e) {
        setError(e.message);
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

  return { messages, loading, error, needsAppSelection, model, sendMessage, reset };
}
