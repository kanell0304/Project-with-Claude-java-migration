package com.digitalhelper.service;

import com.digitalhelper.entity.Guide;
import com.digitalhelper.repository.GuideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GuideService {

    private static final Logger log = LoggerFactory.getLogger(GuideService.class);
    private static final int CACHE_DAYS = 30;

    private static final Map<String, String> APP_HELP_URLS = Map.of(
            "토스",      "https://help.toss.im",
            "카카오뱅크", "https://www.kakaobank.com/Help",
            "신한",      "https://www.shinhan.com/hpe/index.jsp#050101000000",
            "국민",      "https://www.kbstar.com/quics?page=C102050",
            "하나",      "https://www.kebhana.com/cont/help/index.jsp",
            "우리",      "https://www.wooribank.com/wb/DrawWBPHP?pgmId=PCUST0010",
            "농협",      "https://www.nonghyup.com/guide/faq.do"
    );

    private final GuideRepository guideRepository;
    private final OpenAiService openAiService;
    private final KeywordDetector keywordDetector;

    public GuideService(GuideRepository guideRepository, OpenAiService openAiService, KeywordDetector keywordDetector) {
        this.guideRepository = guideRepository;
        this.openAiService = openAiService;
        this.keywordDetector = keywordDetector;
    }

    /** 대화 이력에서 앱/작업 감지 후 가이드 반환. 없으면 null */
    public GuideResult getOrFetchGuide(List<String> userMessages) {
        var detection = keywordDetector.detectFromHistory(userMessages);
        if (!detection.hasBoth()) {
            log.info("[가이드] 앱/작업 키워드 감지 안됨 → 일반 응답");
            return null;
        }

        String appName = detection.app();
        String task = detection.task();
        log.info("[가이드] 감지: 앱={}, 작업={}", appName, task);

        Optional<Guide> cached = guideRepository.findValidGuide(appName, task, LocalDateTime.now());
        if (cached.isPresent()) {
            log.info("[가이드] DB 캐시 히트 → web search 생략");
            return new GuideResult(cached.get().getContent(), cached.get().getSourceUrl());
        }

        log.info("[가이드] 캐시 없음 → web search 시작");
        String content = openAiService.fetchGuideViaSearch(appName, task);
        String sourceUrl = APP_HELP_URLS.get(appName);

        saveOrUpdate(appName, task, content, sourceUrl);
        log.info("[가이드] web search 완료 → DB 저장 (출처: {})", sourceUrl);
        return new GuideResult(content, sourceUrl);
    }

    private void saveOrUpdate(String appName, String task, String content, String sourceUrl) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(CACHE_DAYS);
        Guide guide = guideRepository.findByAppNameAndTask(appName, task)
                .orElse(new Guide(appName, task, content, sourceUrl, expiresAt));
        guide.setContent(content);
        guide.setSourceUrl(sourceUrl);
        guide.setCreatedAt(LocalDateTime.now());
        guide.setExpiresAt(expiresAt);
        guideRepository.save(guide);
    }

    public record GuideResult(String content, String sourceUrl) {}
}
