package login.logintest.service;


import login.logintest.dto.JwtResponseDto;
import login.logintest.dto.KakaoUserInfoResponseDto;
import login.logintest.entity.RefreshToken;
import login.logintest.entity.User;
import login.logintest.repository.RefreshTokenRepository;
import login.logintest.repository.UserRepository;
import login.logintest.token.AuthTokens;
import login.logintest.token.AuthTokensGenerator;
import login.logintest.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoUserService {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokensGenerator authTokensGenerator; // AuthTokensGenerator 의존성 추가


    @Transactional
    public AuthTokens loginOrSignup(String code) {
        // Access Token 가져오기
        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        // 사용자 정보 가져오기
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        // 카카오 ID로 기존 사용자가 있는지 조회
        String kakaoId = userInfo.getId().toString();
        User user = userRepository.findByKakaoId(kakaoId)
                .map(existingUser -> { // 기존 사용자일 경우 프로필 업데이트
                    existingUser.updateProfile(
                            userInfo.getKakaoAccount().getProfile().getNickName(),
                            userInfo.getKakaoAccount().getProfile().getProfileImageUrl()
                    );
                    return existingUser;
                })
                .orElseGet(() -> { // 신규 사용자일 경우 회원가입
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .email(userInfo.getKakaoAccount().getEmail())
                            .nickname(userInfo.getKakaoAccount().getProfile().getNickName())
                            .profileImageUrl(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                            .build();
                    return userRepository.save(newUser);
                });

        // AuthTokens 생성
//        AuthTokens tokens = authTokensGenerator.generate(user.getId().toString());
//
//        // Refresh Token 저장
//        RefreshToken refreshToken = RefreshToken.builder()
//                .token(tokens.getRefreshToken())
//                .userId(user.getId().toString())
//                .expiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14)) // 14일 설정
//                .build();
//        refreshTokenRepository.save(refreshToken);

        //return user;
        AuthTokens tokens = authTokensGenerator.generate(user.getId().toString());

        // Refresh Token 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .token(tokens.getRefreshToken())
                .userId(user.getId().toString())
                .expiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14))
                .build());

        return tokens;
    }





}
