const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export default function LoginScreen() {
  const handleKakaoLogin = () => {
    window.location.href = `${API_URL}/oauth2/authorization/kakao`;
  };

  return (
    <div className="flex flex-col items-center justify-center h-full px-6 text-center gap-8">
      <div className="flex flex-col gap-3">
        <p className="text-3xl font-bold text-gray-800">디지털 도우미</p>
        <p className="text-xl text-gray-500">어르신을 위한 인터넷 뱅킹 도우미입니다</p>
      </div>

      <div className="flex flex-col gap-3 w-full max-w-sm">
        <p className="text-xl font-medium text-gray-700">
          시작하려면 로그인이 필요합니다
        </p>
        <p className="text-base text-gray-400">
          카카오 계정으로 간편하게 로그인하세요
        </p>
      </div>

      <div className="flex flex-col gap-4 w-full max-w-sm">
        <button
          onClick={handleKakaoLogin}
          className="w-full py-5 rounded-2xl text-2xl font-bold transition-colors shadow-md flex items-center justify-center gap-3"
          style={{ backgroundColor: "#FEE500", color: "#191919" }}
        >
          <img
            src="https://developers.kakao.com/assets/img/about/logos/kakaolink/kakaolink_btn_medium.png"
            alt="카카오"
            className="w-8 h-8"
          />
          카카오로 로그인
        </button>
      </div>

      <p className="text-sm text-gray-400 max-w-sm">
        로그인 시 서비스 이용약관 및 개인정보처리방침에 동의하는 것으로 간주됩니다
      </p>
    </div>
  );
}
