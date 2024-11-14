package login.logintest.service;

import login.logintest.entity.RefreshToken;
import login.logintest.repository.RefreshTokenRepository;
import login.logintest.token.AuthTokens;
import login.logintest.token.AuthTokensGenerator;
import login.logintest.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthTokensGenerator authTokensGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthTokens refreshAccessToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Find refresh token in database
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Check if token is expired
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        // Generate new tokens
        AuthTokens newTokens = authTokensGenerator.generate(storedToken.getUserId());

        // Update refresh token in database
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newTokens.getRefreshToken())
                .userId(storedToken.getUserId())
                .expiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14)) // 14 days
                .build();

        refreshTokenRepository.delete(storedToken);
        refreshTokenRepository.save(newRefreshToken);

        return newTokens;
    }

    @Transactional
    public void logout(String accessToken) {
        try {
            String userId = jwtTokenProvider.extractUserId(accessToken);
            refreshTokenRepository.deleteByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Invalid access token");
        }
    }
}