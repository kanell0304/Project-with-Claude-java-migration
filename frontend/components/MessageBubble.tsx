import { ChatMessage } from "@/hooks/useChat";

interface Props {
  message: ChatMessage;
}

export default function MessageBubble({ message }: Props) {
  const isUser = message.role === "user";
  const meta = message.meta;

  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"} mb-4`}>
      {!isUser && (
        <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white text-lg mr-3 shrink-0">
          도
        </div>
      )}
      <div className="flex flex-col max-w-[80%]">
        <div
          className={`px-5 py-4 rounded-2xl text-xl leading-relaxed ${
            isUser
              ? "bg-blue-600 text-white rounded-br-sm"
              : "bg-gray-100 text-gray-900 rounded-bl-sm"
          }`}
        >
          {message.content}
        </div>

        {!isUser && meta && (
          <div className="mt-1 ml-1 flex items-center gap-2 text-sm text-gray-400">
            {meta.guide_used ? (
              meta.source_url ? (
                <a
                  href={meta.source_url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-green-500 hover:text-green-600 hover:underline cursor-pointer"
                >
                  ✓ 공식 가이드 참고
                </a>
              ) : (
                <span className="text-green-500">✓ 공식 가이드 참고</span>
              )
            ) : (
              <span>일반 응답</span>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
