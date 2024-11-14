package login.logintest.controller;

import login.logintest.dto.JwtResponseDto;
import login.logintest.service.TokenService;
import login.logintest.token.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
        try {
            AuthTokens newTokens = tokenService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(new JwtResponseDto(newTokens.getAccessToken(), newTokens.getRefreshToken()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            tokenService.logout(accessToken);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}