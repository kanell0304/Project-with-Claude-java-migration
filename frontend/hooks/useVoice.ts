"use client";

import { useState, useRef, useCallback } from "react";
import { sendVoice } from "@/lib/api";

export function useVoice(onResult: (transcript: string) => void) {
  const [recording, setRecording] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const mediaRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<Blob[]>([]);

  const start = useCallback(async () => {
    setError(null);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream);
      chunksRef.current = [];

      recorder.ondataavailable = (e) => {
        if (e.data.size > 0) chunksRef.current.push(e.data);
      };

      recorder.onstop = async () => {
        stream.getTracks().forEach((t) => t.stop());
        const blob = new Blob(chunksRef.current, { type: "audio/webm" });
        setLoading(true);
        try {
          const res = await sendVoice(blob);
          onResult(res.transcript);
        } catch (e) {
          setError((e as Error).message);
        } finally {
          setLoading(false);
        }
      };

      recorder.start();
      mediaRef.current = recorder;
      setRecording(true);
    } catch {
      setError("마이크 권한이 필요합니다.");
    }
  }, [onResult]);

  const stop = useCallback(() => {
    mediaRef.current?.stop();
    setRecording(false);
  }, []);

  return { recording, loading, error, start, stop };
}
