"use client";

import { useCallback } from "react";
import { useChat } from "@/hooks/useChat";
import { useVoice } from "@/hooks/useVoice";
import ChatWindow from "@/components/ChatWindow";
import InputBar from "@/components/InputBar";
import VendorSelector from "@/components/VendorSelector";

export default function Home() {
  const { messages, loading, error, needsAppSelection, sendMessage, reset } = useChat();

  const handleVoiceResult = useCallback(
    (transcript: string) => {
      sendMessage(transcript);
    },
    [sendMessage]
  );

  const { recording, loading: voiceLoading, error: voiceError, start, stop } = useVoice(
    handleVoiceResult
  );

  const isInputDisabled = loading || voiceLoading;

  return (
    <div className="flex flex-col h-full max-w-2xl mx-auto">
      <header className="flex items-center justify-between px-5 py-4 bg-blue-600 text-white shrink-0">
        <h1 className="text-2xl font-bold">디지털 도우미</h1>
        <button
          onClick={reset}
          className="text-base bg-blue-500 hover:bg-blue-400 px-4 py-2 rounded-xl transition-colors"
        >
          새 대화
        </button>
      </header>

      {/* ChatWindow는 채팅 로딩(loading)만 전달 → 사용자 메시지 표시 후 ... 애니메이션 */}
      <ChatWindow
        messages={messages}
        loading={loading}
        onExampleClick={sendMessage}
        onQuickReply={sendMessage}
      />

      {/* STT 처리 중 안내 */}
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
        onVoiceStop={stop}
        recording={recording}
        loading={isInputDisabled}
      />
    </div>
  );
}
