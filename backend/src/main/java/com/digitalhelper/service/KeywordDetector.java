package com.digitalhelper.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KeywordDetector {

    private static final Map<String, List<String>> APP_KEYWORDS = Map.of(
            "토스",      List.of("toss", "토스"),
            "카카오뱅크", List.of("카카오뱅크", "kakaobank"),
            "신한",      List.of("신한", "sol", "쏠"),
            "국민",      List.of("국민", "kb", "스타뱅킹"),
            "하나",      List.of("하나", "하나은행"),
            "우리",      List.of("우리", "우리은행"),
            "농협",      List.of("농협", "nh")
    );

    private static final Map<String, List<String>> TASK_KEYWORDS = Map.of(
            "계좌이체",    List.of("계좌이체", "송금", "이체", "보내"),
            "잔액조회",    List.of("잔액", "조회", "얼마"),
            "공과금납부",  List.of("공과금", "납부", "요금"),
            "OTP발급",    List.of("otp", "일회용비밀번호"),
            "계좌개설",    List.of("계좌개설", "계좌 만들기", "통장개설", "통장 만들기"),
            "이체한도변경", List.of("이체한도", "송금한도", "한도 변경", "한도변경")
    );

    public String detectApp(String text) {
        String lower = text.toLowerCase();
        for (var entry : APP_KEYWORDS.entrySet()) {
            if (entry.getValue().stream().anyMatch(lower::contains)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String detectTask(String text) {
        String lower = text.toLowerCase();
        for (var entry : TASK_KEYWORDS.entrySet()) {
            if (entry.getValue().stream().anyMatch(lower::contains)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** 대화 이력 전체에서 앱과 작업을 탐색 (최신 메시지 우선) */
    public DetectionResult detectFromHistory(List<String> messages) {
        String detectedApp = null;
        String detectedTask = null;

        for (int i = messages.size() - 1; i >= 0; i--) {
            String msg = messages.get(i);
            if (detectedApp == null) detectedApp = detectApp(msg);
            if (detectedTask == null) detectedTask = detectTask(msg);
            if (detectedApp != null && detectedTask != null) break;
        }

        return new DetectionResult(detectedApp, detectedTask);
    }

    public record DetectionResult(String app, String task) {
        public boolean hasApp() { return app != null; }
        public boolean hasTask() { return task != null; }
        public boolean hasBoth() { return hasApp() && hasTask(); }
    }
}
