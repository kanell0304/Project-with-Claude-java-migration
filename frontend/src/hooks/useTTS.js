import { useRef, useState, useCallback } from "react";

export function useTTS(enabled) {
  const [volume, setVolumeState] = useState(1);
  const [speaking, setSpeaking] = useState(false);

  const volumeRef = useRef(1);
  const currentTextRef = useRef("");
  const isRestartingRef = useRef(false);

  const startSpeech = useCallback((text, vol) => {
    isRestartingRef.current = true;
    window.speechSynthesis.cancel();

    const cleaned = text.replace(/[#*`>~_]/g, "").trim();
    if (!cleaned) return;

    currentTextRef.current = cleaned;

    const utterance = new SpeechSynthesisUtterance(cleaned);
    utterance.lang = "ko-KR";
    utterance.volume = vol;
    utterance.rate = 0.9;

    utterance.onstart = () => {
      isRestartingRef.current = false;
      setSpeaking(true);
    };
    utterance.onend = () => {
      if (isRestartingRef.current) return;
      setSpeaking(false);
      currentTextRef.current = "";
    };
    utterance.onerror = () => {
      if (isRestartingRef.current) return;
      setSpeaking(false);
      currentTextRef.current = "";
    };

    window.speechSynthesis.speak(utterance);
  }, []);

  const speak = useCallback((text) => {
    if (!enabled || !window.speechSynthesis) return;
    startSpeech(text, volumeRef.current);
  }, [enabled, startSpeech]);

  const setVolume = useCallback((val) => {
    volumeRef.current = val;
    setVolumeState(val);
    if (currentTextRef.current) {
      startSpeech(currentTextRef.current, val);
    }
  }, [startSpeech]);

  const stop = useCallback(() => {
    isRestartingRef.current = false;
    currentTextRef.current = "";
    window.speechSynthesis.cancel();
    setSpeaking(false);
  }, []);

  return { speak, stop, speaking, volume, setVolume };
}
