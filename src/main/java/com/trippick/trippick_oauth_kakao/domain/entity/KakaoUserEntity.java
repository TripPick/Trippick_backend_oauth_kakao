package com.trippick.trippick_oauth_kakao.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kakao_user")
public class KakaoUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoId;        // 카카오의 유저 id(이메일)
    private String kakaoName;       
    private String profileImageUrl;

    private String refreshToken;    // 리프레시 토큰
    private LocalDateTime refreshTokenExpireAt;  // 리프레시 토큰 만료 시간

    // builder, getters, setters 등 생략
}