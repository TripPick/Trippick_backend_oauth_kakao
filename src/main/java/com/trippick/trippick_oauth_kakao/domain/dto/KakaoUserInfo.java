package com.trippick.trippick_oauth_kakao.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KakaoUserInfo {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String refreshToken;
    private LocalDateTime refreshTokenExpireAt;

    public KakaoUserInfo(String kakaoId, String kakaoName, String profileImageUrl, String refreshToken, LocalDateTime refreshTokenExpireAt) {
        this.email = kakaoId;
        this.nickname = kakaoName;
        this.profileImageUrl = profileImageUrl;
        this.refreshToken = refreshToken;
        this.refreshTokenExpireAt = refreshTokenExpireAt;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getRefreshTokenExpireAt() {
        return refreshTokenExpireAt;
    }

    @Override
    public String toString() {
        return "KakaoUserInfo{" +
                "email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", refreshTokenExpireAt=" + refreshTokenExpireAt +
                '}';
    }
}