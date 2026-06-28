package com.digitalhelper.config;

import com.digitalhelper.entity.User;
import com.digitalhelper.repository.UserRepository;
import com.digitalhelper.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                JwtService jwtService,
                                @Value("${frontend.url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Object idAttr = oAuth2User.getAttribute("id");
        String kakaoId = idAttr != null ? idAttr.toString() : null;

        // 카카오 프로필 정보 추출
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> profile = kakaoAccount != null
                ? (Map<String, Object>) kakaoAccount.get("profile") : Map.of();

        String nickname = (String) profile.getOrDefault("nickname", "사용자");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        // 신규 유저면 저장, 기존 유저면 프로필 업데이트
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> new User(kakaoId, nickname, email));
        user.updateProfile(nickname, email);
        userRepository.save(user);

        String token = jwtService.generate(kakaoId, nickname);

        // 프론트엔드로 JWT 전달 (쿼리 파라미터)
        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/auth/callback?token=" + token);
    }
}
