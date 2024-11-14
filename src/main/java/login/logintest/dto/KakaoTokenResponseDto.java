package login.logintest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기본 생성자 (@NoArgsConstructor)
 * @NoArgsConstructor 어노테이션은 파라미터가 없는 기본 생성자를 자동으로 생성해줍니다.
// * 역직렬화 과정에서 Jackson 같은 JSON 라이브러리는 먼저 빈 객체를 생성한 후 JSON 데이터를 필드에 하나씩 채워 넣습니다.
 * 이 과정에서 기본 생성자가 필요하며, @NoArgsConstructor는 이를 자동으로 만들어 줍니다.
 * @JsonProperty 어노테이션 : 명시적으로 매핑해 주는 것이 필요하기 떄문에 사용
 * 자세한 API정보 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token
 */
@Getter
@NoArgsConstructor //역직렬화를 위한 기본 생성자
@JsonIgnoreProperties(ignoreUnknown = true)//응답 JSON에 포함되지 않은 필드는 무시됩니다.
public class KakaoTokenResponseDto {

    @JsonProperty("token_type")
    public String tokenType;
    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("id_token")
    public String idToken;
    @JsonProperty("expires_in")
    public Integer expiresIn;
    @JsonProperty("refresh_token")
    public String refreshToken;
    @JsonProperty("refresh_token_expires_in")
    public Integer refreshTokenExpiresIn;
    @JsonProperty("scope")
    public String scope;
}