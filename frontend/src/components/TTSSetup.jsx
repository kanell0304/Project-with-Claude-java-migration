export default function TTSSetup({ onSelect }) {
  return (
    <div className="flex flex-col items-center justify-center h-full px-6 text-center gap-8">
      <div className="flex flex-col gap-3">
        <p className="text-3xl font-bold text-gray-800">디지털 도우미</p>
        <p className="text-xl text-gray-500">어르신을 위한 인터넷 뱅킹 도우미입니다</p>
      </div>

      <div className="flex flex-col gap-3 w-full max-w-sm">
        <p className="text-xl font-medium text-gray-700">
          AI의 답변을 소리로 읽어드릴까요?
        </p>
        <p className="text-base text-gray-400">
          화면의 글씨를 읽기 어려우신 분께 추천드립니다
        </p>
      </div>

      <div className="flex flex-col gap-4 w-full max-w-sm">
        <button
          onClick={() => onSelect(true)}
          className="w-full py-5 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white text-2xl font-bold rounded-2xl transition-colors shadow-md"
        >
          🔊 음성 안내 사용
        </button>
        <button
          onClick={() => onSelect(false)}
          className="w-full py-5 bg-gray-100 hover:bg-gray-200 active:bg-gray-300 text-gray-700 text-2xl font-bold rounded-2xl transition-colors"
        >
          🔇 사용 안함
        </button>
      </div>

      <p className="text-base text-gray-400">
        나중에 새 대화 버튼을 누르면 다시 선택할 수 있어요
      </p>
    </div>
  );
}
