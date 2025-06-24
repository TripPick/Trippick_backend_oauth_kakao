package com.trippick.trippick_oauth_kakao.controller;

import com.trippick.trippick_oauth_kakao.domain.dto.KakaoUserInfo;
import com.trippick.trippick_oauth_kakao.service.KakaoLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/user/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;

    /**
     * 테스트용 엔드포인트
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("테스트 엔드포인트 호출됨");
        return ResponseEntity.ok("테스트 성공!");
    }

    /**
     * 사용자 정보 조회
     */
    @GetMapping("/user/info")
    public ResponseEntity<?> getUserInfo(@RequestParam String userId) {
        log.info("사용자 정보 조회 요청: {}", userId);
        
        try {
            KakaoUserInfo userInfo = kakaoLoginService.getUserInfoByKakaoId(userId);
            
            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "사용자를 찾을 수 없습니다."));
            }
            
            Map<String, Object> response = Map.of(
                "userId", userInfo.getEmail(),
                "userName", userInfo.getNickname(),
                "profileImageUrl", userInfo.getProfileImageUrl()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 카카오 OAuth2 콜백 처리
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code, @RequestParam(required = false) String error) {
        log.info("카카오 OAuth2 콜백 호출됨");
        log.info("인증 코드: {}", code);
        log.info("에러 파라미터: {}", error);
        
        try {
            if (error != null) {
                log.error("카카오 OAuth2 에러 발생: {}", error);
                String errorRedirectUrl = "http://localhost:5173/login?error=" + error;
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", errorRedirectUrl)
                        .build();
            }
            
            if (code == null || code.isBlank()) {
                log.error("인증 코드가 없습니다.");
                String errorRedirectUrl = "http://localhost:5173/login?error=no_code";
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", errorRedirectUrl)
                        .build();
            }

            log.info("JWT 토큰 생성 시작...");
            String jwtToken = kakaoLoginService.kakaoLogin(code);
            log.info("JWT 토큰 생성 완료");

            if (jwtToken == null) {
                log.error("JWT 토큰 생성 실패");
                String errorRedirectUrl = "http://localhost:5173/login?error=jwt_failed";
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", errorRedirectUrl)
                        .build();
            }

            String redirectUrl = "http://localhost:5173/login?token=" + jwtToken;
            log.info("프론트엔드로 리다이렉트: {}", redirectUrl);
            
            // 프론트엔드로 리다이렉트하면서 토큰 전달
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();

        } catch (Exception e) {
            log.error("카카오 OAuth2 콜백 처리 중 예외 발생", e);
            String errorRedirectUrl = "http://localhost:5173/login?error=server_error&message=" + e.getMessage();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", errorRedirectUrl)
                    .build();
        }
    }

    /**
     * 기존 POST 방식 (JavaScript SDK용) - 호환성을 위해 유지
     */
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        try {
            String accessToken = body.get("accessToken");

            if (accessToken == null || accessToken.isBlank()) {
                return ResponseEntity.badRequest().body("accessToken 누락됨");
            }

            String jwtToken = kakaoLoginService.loginWithKakao(accessToken);

            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT 발급 실패");
            }

            return ResponseEntity.ok(Collections.singletonMap("token", jwtToken));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류 발생: " + e.getMessage());
        }
    }
}