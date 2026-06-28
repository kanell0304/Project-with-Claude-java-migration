import { useEffect, useRef, useState } from "react";
import { getTasks } from "../lib/api";
import MessageBubble from "./MessageBubble";

const QUICK_REPLIES = ["다음", "확인했어요", "완료했어요", "다시 설명해주세요"];

export default function ChatWindow({ messages, loading, onExampleClick, onQuickReply, token }) {
  const bottomRef = useRef(null);
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    getTasks(token).then(setTasks).catch(() => setTasks([]));
  }, [token]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const lastMessage = messages[messages.length - 1];
  const showQuickReplies = !loading && lastMessage?.role === "assistant";

  return (
    <div className="flex-1 overflow-y-auto px-4 py-6">
      {messages.length === 0 && (
        <div className="flex flex-col items-center justify-center h-full text-center gap-4">
          <p className="text-2xl font-medium text-gray-600">안녕하세요!</p>
          <p className="text-xl text-gray-400">어떤 업무를 도와드릴까요?</p>
          <div className="mt-2 space-y-3 w-full max-w-sm">
            {tasks.length > 0 ? (
              tasks.map((task) => (
                <button
                  key={task.name}
                  onClick={() => onExampleClick(`${task.display_name} 방법을 알려주세요`)}
                  disabled={loading}
                  className="w-full px-4 py-4 bg-blue-50 hover:bg-blue-100 active:bg-blue-200 rounded-xl text-blue-700 text-lg text-left transition-colors disabled:opacity-40"
                >
                  {task.display_name}
                </button>
              ))
            ) : (
              <p className="text-gray-400 text-base">업무 목록을 불러오는 중...</p>
            )}
            <p className="text-base text-gray-400 pt-2">
              원하시는 내용이 없으면 아래에 직접 입력해 주세요
            </p>
          </div>
        </div>
      )}

      {messages.map((msg, i) => (
        <MessageBubble key={i} message={msg} />
      ))}

      {showQuickReplies && (
        <div className="flex flex-wrap gap-2 mt-1 ml-14 mb-2">
          {QUICK_REPLIES.map((text) => (
            <button
              key={text}
              onClick={() => onQuickReply(text)}
              className="px-4 py-2 border-2 border-blue-300 text-blue-600 rounded-full text-base font-medium hover:bg-blue-50 active:bg-blue-100 transition-colors"
            >
              {text}
            </button>
          ))}
        </div>
      )}

      {loading && (
        <div className="flex justify-start mb-4">
          <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white text-lg mr-3 shrink-0">
            도
          </div>
          <div className="bg-gray-100 px-5 py-4 rounded-2xl rounded-bl-sm flex gap-1 items-center">
            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:0ms]" />
            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:150ms]" />
            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:300ms]" />
          </div>
        </div>
      )}

      <div ref={bottomRef} />
    </div>
  );
}
