package com.digitalhelper.service;

import com.digitalhelper.dto.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private static final String BASE_SYSTEM_PROMPT = """
            당신은 디지털 기기 사용이 어려운 어르신들을 위한 인터넷 뱅킹 전용 도우미입니다.

            역할:
            - 인터넷 뱅킹(계좌이체, 공과금 납부, OTP 발급 등) 사용 방법을 단계별로 안내합니다.
            - 금융 서류(신청서, 확인서 등) 작성 방법을 쉽게 설명합니다.

            반드시 지켜야 할 규칙:
            - 인터넷 뱅킹, 금융 앱 사용, 금융 서류 작성과 무관한 질문에는 절대 답변하지 않습니다.
            - 금융과 무관한 질문을 받으면 반드시 다음과 같이 안내합니다:
              "저는 인터넷 뱅킹과 금융 업무만 도와드릴 수 있어요. 다른 질문은 답변이 어렵습니다."
            - 어려운 용어는 반드시 쉬운 말로 풀어 설명합니다.
            - 한 번에 하나의 단계만 안내합니다.
            - 사용자가 "다음", "했어요", "눌렀어요" 등이라고 하면 다음 단계를 안내합니다.
            - 금융 정보의 정확성이 중요한 경우, 직접 은행에 문의하도록 안내합니다.
            - 주민등록번호 등 민감한 개인정보는 절대 입력하지 말라고 안내합니다.
            - 항상 친절하고 천천히, 이해하기 쉬운 말로 답변합니다.
            - 답변은 짧고 명확하게 작성합니다 (한 번에 너무 많은 정보를 주지 않습니다).
            """;

    // 명백한 악용 키워드 — GPT 호출 없이 즉시 차단
    private static final List<String> BLOCKED_KEYWORDS = List.of(
            "섹스", "야동", "성관계", "포르노", "도박", "불법", "해킹", "마약",
            "폭탄", "살인", "자살", "테러", "욕설", "씨발", "개새끼", "병신"
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    public OpenAiService(RestClient openAiRestClient, ObjectMapper objectMapper) {
        this.restClient = openAiRestClient;
        this.objectMapper = objectMapper;
    }

    public String getChatResponse(List<Message> messages, String guide) {
        // 키워드 필터 — GPT 호출 없이 즉시 차단
        String lastUserMessage = messages.stream()
                .filter(m -> "user".equals(m.role()))
                .reduce((first, second) -> second)
                .map(m -> m.content())
                .orElse("");

        boolean blocked = BLOCKED_KEYWORDS.stream()
                .anyMatch(keyword -> lastUserMessage.contains(keyword));

        if (blocked) {
            return "죄송합니다. 해당 내용은 답변드리기 어렵습니다. 인터넷 뱅킹이나 금융 업무에 대해 질문해 주세요.";
        }

        String systemContent = guide == null
                ? BASE_SYSTEM_PROMPT
                : BASE_SYSTEM_PROMPT + "\n\n[참고 가이드]\n" + guide + "\n위 가이드를 반드시 참고하여 정확하게 안내하세요.";

        List<Map<String, String>> formatted = new ArrayList<>();
        formatted.add(Map.of("role", "system", "content", systemContent));
        for (Message m : messages) {
            formatted.add(Map.of("role", m.role(), "content", m.content()));
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", formatted,
                "max_completion_tokens", 1000
        );

        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return response.path("choices").get(0).path("message").path("content").asText();
    }

    /** OpenAI Responses API (web_search_preview 툴 사용) */
    public String fetchGuideViaSearch(String appName, String task) {
        String query = appName + " 앱 " + task + " 방법 단계별 안내";

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "tools", List.of(Map.of("type", "web_search_preview")),
                "input", query
        );

        JsonNode response = restClient.post()
                .uri("/responses")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        // output 배열에서 type=message 인 항목의 텍스트 추출
        for (JsonNode item : response.path("output")) {
            if ("message".equals(item.path("type").asText())) {
                JsonNode contentArr = item.path("content");
                if (contentArr.isArray() && !contentArr.isEmpty()) {
                    return contentArr.get(0).path("text").asText();
                }
            }
        }

        // fallback: output_text 필드
        String outputText = response.path("output_text").asText(null);
        if (outputText != null && !outputText.isBlank()) return outputText;

        log.warn("[OpenAI] web_search_preview 결과에서 텍스트 추출 실패. 원본: {}", response);
        return "";
    }

    /** Whisper STT */
    public String transcribeAudio(byte[] audioBytes, String filename, String contentType) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() { return filename; }
        });
        body.add("model", "whisper-1");
        body.add("language", "ko");

        JsonNode response = restClient.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return response.path("text").asText();
    }
}
