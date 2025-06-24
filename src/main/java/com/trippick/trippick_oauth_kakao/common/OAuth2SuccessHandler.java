package com.trippick.trippick_oauth_kakao.common;

import com.trippick.trippick_oauth_kakao.service.KakaoLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final KakaoLoginService kakaoLoginService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 인가 코드 가져오기
        String code = request.getParameter("code");
        if (code == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Authorization code not found.");
            return;
        }

        try {
            // KakaoLoginService를 이용하여 로그인 처리 및 JWT 발급
            // String jwtToken = kakaoLoginService.kakaoLogin(code);

            // JWT 토큰을 쿼리 파라미터로 메인 페이지에 리다이렉트
            // String redirectUrl = "http://localhost:5173/?token=" + jwtToken;

            // response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 login failed");
        }
    }
}
