package login.logintest.service;

import io.netty.handler.codec.http.HttpHeaderValues;
import login.logintest.dto.KakaoTokenResponseDto;
import login.logintest.dto.KakaoUserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 요청 이렇게 하기 위함
 * curl -v -X POST "https://kauth.kakao.com/oauth/token" \ -H "Content-Type: application/x-www-form-urlencoded" \ -d "grant_type=authorization_code" \ -d "client_id=${REST_API_KEY}" \ --data-urlencode "redirect_uri=${REDIRECT_URI}" \ -d "code=${AUTHORIZE_CODE}"
 * 응답 값
 * HTTP/1.1 200 OK
 * Content-Type: application/json;charset=UTF-8
 * {
 *     "token_type":"bearer",
 *     "access_token":"${ACCESS_TOKEN}",
 *     "expires_in":43199,
 *     "refresh_token":"${REFRESH_TOKEN}",
 *     "refresh_token_expires_in":5184000,
 *     "scope":"account_email profile"
 * }
 *
 */
@Slf4j
@Service
public class KakaoService {
    private String clientId;
    private final String KAUTH_TOKEN_URL_HOST;
    private final String KAUTH_USER_URL_HOST;

    @Autowired
    public KakaoService(@Value("${kakao.client_id}") String clientId) {
        this.clientId = clientId;
        KAUTH_TOKEN_URL_HOST ="https://kauth.kakao.com";
        KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    }

    public String getAccessTokenFromKakao(String code){
        KakaoTokenResponseDto kakaoTokenResponseDto
                = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")//https://kauth.kakao.com/oauth/token URL로 POST 요청을 보내면, 토큰을 받을 수 있다.
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build(true))//url 빌드를 완료
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //예외를 커스텀해서 넣을 수 있음 onStatus로
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDto.class)//JSON 응답을 KakaoTokenResponseDto 객체로 변환
                .block();

        log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.getRefreshToken());
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoTokenResponseDto.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDto.getScope());

        return kakaoTokenResponseDto.getAccessToken();
    }

    /**
     *헤더에 "Bearer " + accessToken으로 get방식 보내면 ResponseBody받게됨(내가 동의한 부분에 따라 달라짐)
     * 동의 안한 부분은 null로 채워짐
     * response body example
     * HTTP/1.1 200 OK
     * {
     *     "id":123456789,
     *     "connected_at": "2022-04-11T01:45:28Z",
     *     "kakao_account": {
     *         "profile_nickname_needs_agreement": false,
     *         "profile": {
     *             "nickname": "홍길동"
     *         }
     *     },
     *     "properties":{
     *         "${CUSTOM_PROPERTY_KEY}": "${CUSTOM_PROPERTY_VALUE}",
     *         ...
     *     }
     * }
     *
     */
    public KakaoUserInfoResponseDto getUserInfo(String accessToken){
        KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //예외 처리(구체화시킬수도 있음)
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());

        return userInfo;

    }





}
