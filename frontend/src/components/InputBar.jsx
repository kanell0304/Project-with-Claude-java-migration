import { useState } from "react";

export default function InputBar({ onSend, onVoiceStart, onVoiceStop, recording, loading }) {
  const [text, setText] = useState("");

  const handleSend = () => {
    const trimmed = text.trim();
    if (!trimmed || loading) return;
    onSend(trimmed);
    setText("");
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="px-4 py-4 bg-white border-t border-gray-200">
      <div className="flex items-end gap-3 max-w-2xl mx-auto">
        <textarea
          className="flex-1 resize-none rounded-2xl border border-gray-300 px-4 py-3 text-xl leading-relaxed focus:outline-none focus:border-blue-500 min-h-[56px] max-h-36"
          placeholder="여기에 질문을 입력하세요"
          rows={1}
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={loading || recording}
        />

        <button
          onClick={recording ? onVoiceStop : onVoiceStart}
          disabled={loading}
          className={`w-14 h-14 rounded-full flex items-center justify-center text-2xl shrink-0 transition-colors ${
            recording
              ? "bg-red-500 text-white animate-pulse"
              : "bg-gray-100 text-gray-600 hover:bg-gray-200"
          }`}
          aria-label={recording ? "녹음 중지" : "음성 입력"}
        >
          {recording ? "■" : "🎤"}
        </button>

        <button
          onClick={handleSend}
          disabled={!text.trim() || loading || recording}
          className="w-14 h-14 rounded-full bg-blue-600 text-white flex items-center justify-center text-2xl shrink-0 disabled:opacity-40 hover:bg-blue-700 transition-colors"
          aria-label="전송"
        >
          ▶
        </button>
      </div>
    </div>
  );
}
