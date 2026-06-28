import { useCallback, useEffect, useRef, useState } from "react";
import { useChat } from "./hooks/useChat";
import { useVoice } from "./hooks/useVoice";
import { useTTS } from "./hooks/useTTS";
import ChatWindow from "./components/ChatWindow";
import InputBar from "./components/InputBar";
import VendorSelector from "./components/VendorSelector";
import TTSSetup from "./components/TTSSetup";

export default function App() {
  const [ttsEnabled, setTtsEnabled] = useState(null);
  const [volumeTooltip, setVolumeTooltip] = useState({ visible: false, x: 0, y: 0 });
  const tooltipTimerRef = useRef(null);

  const { messages, loading, error, needsAppSelection, model, sendMessage, reset } = useChat();
  const { speak, stop, speaking, volume, setVolume } = useTTS(ttsEnabled === true);

  const prevMessageCountRef = useRef(0);

  // 새 assistant 메시지가 오면 자동으로 읽어줌
  useEffect(() => {
    if (!ttsEnabled) return;
    const lastMsg = messages[messages.length - 1];
    if (messages.length > prevMessageCountRef.current && lastMsg?.role === "assistant") {
      speak(lastMsg.content);
    }
    prevMessageCountRef.current = messages.length;
  }, [messages, ttsEnabled, speak]);

  const handleVoiceResult = useCallback(
    (transcript) => sendMessage(transcript),
    [sendMessage]
  );

  const { recording, loading: voiceLoading, error: voiceError, start, stop: stopVoice } = useVoice(handleVoiceResult);

  const isInputDisabled = loading || voiceLoading;

  const mousePosRef = useRef({ x: 0, y: 0 });

  const handleVolumeChange = (e) => {
    setVolume(Number(e.target.value));
    setVolumeTooltip({ visible: true, x: mousePosRef.current.x, y: mousePosRef.current.y });
    clearTimeout(tooltipTimerRef.current);
    tooltipTimerRef.current = setTimeout(() => {
      setVolumeTooltip((prev) => ({ ...prev, visible: false }));
    }, 1500);
  };

  const handleVolumeMouseMove = (e) => {
    mousePosRef.current = { x: e.clientX, y: e.clientY };
  };

  const handleReset = () => {
    reset();
    stop();
    setTtsEnabled(null);
    prevMessageCountRef.current = 0;
  };

  // TTS 선택 전: 선택 화면
  if (ttsEnabled === null) {
    return (
      <div className="flex flex-col h-full max-w-2xl mx-auto">
        <TTSSetup onSelect={setTtsEnabled} />
      </div>
    );
  }

  // 선택 후: 기존 채팅 화면
  return (
    <div className="flex flex-col h-full max-w-2xl mx-auto">
      {volumeTooltip.visible && (
        <div
          className="fixed z-50 px-3 py-1.5 bg-gray-800 text-white text-sm rounded-lg pointer-events-none whitespace-nowrap"
          style={{ left: volumeTooltip.x + 12, top: volumeTooltip.y - 36 }}
        >
          다음 텍스트부터 적용됩니다
        </div>
      )}
      <header className="flex items-center justify-between px-5 py-4 bg-blue-600 text-white shrink-0">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-bold">디지털 도우미</h1>
          {model && (
            <span className="flex items-center gap-1 text-xs text-blue-100">
              <span className="w-1.5 h-1.5 rounded-full bg-green-400 inline-block" />
              {model}
            </span>
          )}
        </div>

        <div className="flex items-center gap-3">
          {ttsEnabled && (
            <div className="flex items-center gap-2">
              {speaking && (
                <button
                  onClick={stop}
                  className="text-sm bg-blue-500 hover:bg-blue-400 px-3 py-1.5 rounded-lg transition-colors"
                  aria-label="읽기 중지"
                >
                  ■ 정지
                </button>
              )}
              <div className="flex items-center gap-1.5">
                <span className="text-lg">🔊</span>
                <input
                  type="range"
                  min="0"
                  max="1"
                  step="0.05"
                  value={volume}
                  onChange={handleVolumeChange}
                  onMouseMove={handleVolumeMouseMove}
                  className="w-20 accent-white cursor-pointer"
                  aria-label="음량 조절"
                />
              </div>
            </div>
          )}
          <button
            onClick={handleReset}
            className="text-base bg-blue-500 hover:bg-blue-400 px-4 py-2 rounded-xl transition-colors"
          >
            새 대화
          </button>
        </div>
      </header>

      <ChatWindow
        messages={messages}
        loading={loading}
        onExampleClick={sendMessage}
        onQuickReply={sendMessage}
      />

      {voiceLoading && (
        <div className="mx-4 mb-2 px-4 py-3 bg-blue-50 text-blue-600 rounded-xl text-lg flex items-center gap-2">
          <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce [animation-delay:0ms]" />
          <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce [animation-delay:150ms]" />
          <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce [animation-delay:300ms]" />
          <span>음성 인식 중...</span>
        </div>
      )}

      {(error || voiceError) && (
        <div className="mx-4 mb-2 px-4 py-3 bg-red-50 text-red-600 rounded-xl text-lg">
          {error ?? voiceError}
        </div>
      )}

      {needsAppSelection && !isInputDisabled && (
        <VendorSelector onSelect={(name) => sendMessage(name)} />
      )}

      <InputBar
        onSend={sendMessage}
        onVoiceStart={start}
        onVoiceStop={stopVoice}
        recording={recording}
        loading={isInputDisabled}
      />
    </div>
  );
}
