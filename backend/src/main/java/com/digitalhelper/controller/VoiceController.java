package com.digitalhelper.controller;

import com.digitalhelper.dto.VoiceResponse;
import com.digitalhelper.service.OpenAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "audio/webm", "audio/mp4", "audio/mpeg", "audio/wav", "audio/ogg"
    );

    private final OpenAiService openAiService;

    public VoiceController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping
    public ResponseEntity<VoiceResponse> voiceStt(@RequestParam("audio") MultipartFile audio) {
        if (!ALLOWED_TYPES.contains(audio.getContentType())) {
            throw new ResponseStatusException(BAD_REQUEST, "지원하지 않는 오디오 형식입니다.");
        }

        try {
            byte[] bytes = audio.getBytes();
            String filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.webm";
            String transcript = openAiService.transcribeAudio(bytes, filename, audio.getContentType());
            return ResponseEntity.ok(new VoiceResponse(transcript));
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "음성 인식 오류: " + e.getMessage());
        }
    }
}
