import { useEffect, useState } from "react";
import { getVendors } from "../lib/api";

export default function VendorSelector({ onSelect, token }) {
  const [vendors, setVendors] = useState([]);
  const [customInput, setCustomInput] = useState("");
  const [showInput, setShowInput] = useState(false);

  useEffect(() => {
    getVendors("금융", token).then(setVendors).catch(() => setVendors([]));
  }, [token]);

  const handleCustomSubmit = () => {
    const trimmed = customInput.trim();
    if (!trimmed) return;
    onSelect(trimmed);
    setCustomInput("");
    setShowInput(false);
  };

  return (
    <div className="mx-4 mb-4 p-4 bg-blue-50 rounded-2xl">
      <p className="text-lg font-medium text-gray-700 mb-3">
        어떤 앱/서비스를 사용하고 계신가요?
      </p>
      <div className="flex flex-wrap gap-2 mb-3">
        {vendors.map((v) => (
          <button
            key={v.name}
            onClick={() => onSelect(v.name)}
            className="px-4 py-2 bg-white border border-blue-200 rounded-xl text-blue-700 text-base hover:bg-blue-100 active:bg-blue-200 transition-colors"
          >
            {v.name}
          </button>
        ))}
      </div>

      {!showInput ? (
        <button
          onClick={() => setShowInput(true)}
          className="text-base text-gray-500 underline"
        >
          목록에 없어요 (직접 입력)
        </button>
      ) : (
        <div className="flex gap-2 mt-2">
          <input
            type="text"
            value={customInput}
            onChange={(e) => setCustomInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCustomSubmit()}
            placeholder="앱 이름을 입력하세요"
            className="flex-1 px-4 py-2 rounded-xl border border-gray-300 text-lg focus:outline-none focus:border-blue-500"
            autoFocus
          />
          <button
            onClick={handleCustomSubmit}
            className="px-4 py-2 bg-blue-600 text-white rounded-xl text-base hover:bg-blue-700"
          >
            확인
          </button>
        </div>
      )}
    </div>
  );
}
