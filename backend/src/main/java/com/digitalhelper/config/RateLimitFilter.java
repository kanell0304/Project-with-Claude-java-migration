package com.digitalhelper.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // IP별 버킷 저장소
    private final ConcurrentHashMap<String, Bucket> hourlyBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> dailyBuckets = new ConcurrentHashMap<>();

    private Bucket hourlyBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(30)
                        .refillGreedy(30, Duration.ofHours(1))
                        .build())
                .build();
    }

    private Bucket dailyBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillGreedy(100, Duration.ofDays(1))
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // /api/v1/chat, /api/v1/voice 요청만 제한
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/chat") && !path.startsWith("/api/v1/voice")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 로그인 사용자는 계정 기준, 비로그인은 IP 기준
        String principal = null;
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            principal = "user:" + auth.getPrincipal().toString();
        }
        String key = principal != null ? principal : "ip:" + getClientIp(request);

        Bucket hourly = hourlyBuckets.computeIfAbsent(key, k -> hourlyBucket());
        Bucket daily = dailyBuckets.computeIfAbsent(key, k -> dailyBucket());

        if (!hourly.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"잠시 후 다시 시도해 주세요. (1시간 최대 30회)\"}");
            return;
        }

        if (!daily.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"오늘 사용 한도에 도달했습니다. 내일 다시 이용해 주세요. (하루 최대 100회)\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
