package somsomcore.zipcheck.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import somsomcore.zipcheck.domain.auth.dto.SocialUserInfoDto;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public SocialUserInfoDto getSocialUserInfo(String accessToken, OauthType provider) {
		return switch (provider) {
			case KAKAO -> getKakaoUserInfo(accessToken);
			case NAVER -> getNaverUserInfo(accessToken);
		};
    }

    private SocialUserInfoDto getKakaoUserInfo(String accessToken) {
        try {
            WebClient webClient = webClientBuilder.build();
            String response = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            String email = jsonNode.path("kakao_account").path("email").asText();
            String name = jsonNode.path("properties").path("nickname").asText();
            String providerId = jsonNode.path("id").asText();

            return SocialUserInfoDto.builder()
                    .email(email)
                    .name(name)
                    .provider(OauthType.KAKAO)
                    .providerId(providerId)
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Failed to get Kakao user info: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_FAILED);
        } catch (Exception e) {
            log.error("Error parsing Kakao user info: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_FAILED);
        }
    }

    private SocialUserInfoDto getNaverUserInfo(String accessToken) {
        try {
            WebClient webClient = webClientBuilder.build();
            String response = webClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode responseNode = jsonNode.path("response");

            String email = responseNode.path("email").asText();
            String name = responseNode.path("name").asText();
            String providerId = responseNode.path("id").asText();

            return SocialUserInfoDto.builder()
                    .email(email)
                    .name(name)
                    .provider(OauthType.NAVER)
                    .providerId(providerId)
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Failed to get Naver user info: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_FAILED);
        } catch (Exception e) {
            log.error("Error parsing Naver user info: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_FAILED);
        }
    }
}
