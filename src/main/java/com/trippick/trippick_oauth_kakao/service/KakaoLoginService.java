package com.trippick.trippick_oauth_kakao.service;

import com.trippick.trippick_oauth_kakao.domain.dto.KakaoUserInfo;
import com.trippick.trippick_oauth_kakao.domain.entity.KakaoUserEntity;
import com.trippick.trippick_oauth_kakao.domain.repository.KakaoUserRepository;
import com.trippick.trippick_oauth_kakao.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoLoginService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoUserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * OAuth2 인증 코드를 사용하여 카카오 로그인 처리
     */
    public String kakaoLogin(String code) {
        // 1. 인증 코드로 액세스 토큰과 리프레시 토큰 요청
        Map<String, Object> tokenResponse = getAccessToken(code);
        String accessToken = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Integer refreshTokenExpiresIn = (Integer) tokenResponse.get("refresh_token_expires_in");
        
        // 2. 액세스 토큰으로 사용자 정보 조회 및 로그인 처리
        return loginWithKakao(accessToken, refreshToken, refreshTokenExpiresIn);
    }

    /**
     * 액세스 토큰을 사용하여 카카오 로그인 처리
     */
    public String loginWithKakao(String accessToken) {
        // 1. 사용자 정보 조회
        KakaoUserInfo userInfo = getUserInfo(accessToken);

        // 2. 사용자 등록 or 로그인 처리
        KakaoUserEntity user = userRepository.findByKakaoId(userInfo.getEmail());
        if (user == null) {
            user = userRepository.save(
                    KakaoUserEntity.builder()
                            .kakaoId(userInfo.getEmail())
                            .kakaoName(userInfo.getNickname())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .build());
        }

        // 3. JWT 발급
        return jwtUtil.createToken(user.getKakaoId());
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 사용하여 카카오 로그인 처리
     */
    public String loginWithKakao(String accessToken, String refreshToken, Integer refreshTokenExpiresIn) {
        // 1. 사용자 정보 조회
        KakaoUserInfo userInfo = getUserInfo(accessToken);

        // 2. 리프레시 토큰 만료 시간 계산
        LocalDateTime refreshTokenExpireAt = LocalDateTime.now().plusSeconds(refreshTokenExpiresIn);

        // 3. 사용자 등록 or 로그인 처리
        KakaoUserEntity user = userRepository.findByKakaoId(userInfo.getEmail());
        if (user == null) {
            user = userRepository.save(
                    KakaoUserEntity.builder()
                            .kakaoId(userInfo.getEmail())
                            .kakaoName(userInfo.getNickname())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .refreshToken(refreshToken)
                            .refreshTokenExpireAt(refreshTokenExpireAt)
                            .build());
        } else {
            // 기존 사용자의 리프레시 토큰 업데이트
            user.setRefreshToken(refreshToken);
            user.setRefreshTokenExpireAt(refreshTokenExpireAt);
            userRepository.save(user);
        }

        // 4. JWT 발급
        return jwtUtil.createToken(user.getKakaoId());
    }

    /**
     * 인증 코드로 액세스 토큰과 리프레시 토큰 요청
     */
    private Map<String, Object> getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "0eaeed688e2125753c1a0ebf4df023be");
        params.add("client_secret", "R8SJIrR9vC8lipNifZFSD93q9NxRRpXc");
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:8085/api/user/v1/auth/kakao/callback");
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("액세스 토큰 요청 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("액세스 토큰 요청 중 예외 발생", e);
        }
    }

    // 사용자 정보 요청
    private KakaoUserInfo getUserInfo(String accessToken) {
        String requestUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();

                Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                String email = (String) kakaoAccount.get("email");
                String nickname = (String) profile.get("nickname");
                String profileImageUrl = (String) profile.get("profile_image_url");

                return new KakaoUserInfo(email, nickname, profileImageUrl, null, null);
            } else {
                throw new RuntimeException("카카오 사용자 정보 요청 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("카카오 사용자 정보 요청 중 예외 발생", e);
        }
    }

    /**
     * 카카오 ID로 사용자 정보 조회
     */
    public KakaoUserInfo getUserInfoByKakaoId(String kakaoId) {
        KakaoUserEntity user = userRepository.findByKakaoId(kakaoId);
        
        if (user == null) {
            return null;
        }
        
        return new KakaoUserInfo(
            user.getKakaoId(),
            user.getKakaoName(),
            user.getProfileImageUrl(),
            user.getRefreshToken(),
            user.getRefreshTokenExpireAt()
        );
    }
}
