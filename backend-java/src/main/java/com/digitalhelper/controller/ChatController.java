package com.digitalhelper.controller;

import com.digitalhelper.dto.ChatRequest;
import com.digitalhelper.dto.ChatResponse;
import com.digitalhelper.service.GuideService;
import com.digitalhelper.service.KeywordDetector;
import com.digitalhelper.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final OpenAiService openAiService;
    private final GuideService guideService;
    private final KeywordDetector keywordDetector;

    public ChatController(OpenAiService openAiService, GuideService guideService, KeywordDetector keywordDetector) {
        this.openAiService = openAiService;
        this.guideService = guideService;
        this.keywordDetector = keywordDetector;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.messages() == null || request.messages().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "messages가 비어 있습니다.");
        }

        try {
            List<String> userMessages = request.messages().stream()
                    .filter(m -> "user".equals(m.role()))
                    .map(m -> m.content())
                    .toList();

            // 작업은 감지됐지만 앱이 없으면 업체 선택 요청
            boolean taskDetected = userMessages.stream().anyMatch(m -> keywordDetector.detectTask(m) != null);
            boolean appDetected = userMessages.stream().anyMatch(m -> keywordDetector.detectApp(m) != null);
            boolean needsAppSelection = taskDetected && !appDetected;

            GuideService.GuideResult guideResult = guideService.getOrFetchGuide(userMessages);
            String guideContent = guideResult != null ? guideResult.content() : null;
            String sourceUrl = guideResult != null ? guideResult.sourceUrl() : null;

            String reply = openAiService.getChatResponse(request.messages(), guideContent);
            String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

            return ResponseEntity.ok(new ChatResponse(reply, sessionId, guideContent != null, sourceUrl, needsAppSelection));
        } catch (Exception e) {
            log.error("chat 처리 중 오류 발생", e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
