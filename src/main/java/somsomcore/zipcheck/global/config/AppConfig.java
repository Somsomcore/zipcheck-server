package somsomcore.zipcheck.global.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper; // Jackson XML Mapper 사용
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    // 🚨 중요: Base URL을 공통 상위 경로로 변경!
    private static final String API_BASE_URL = "http://apis.data.go.kr/1613000";

    @Bean
    public WebClient webClient() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(xmlMapper, MediaType.APPLICATION_XML));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML));

                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                }).build();

        return WebClient.builder()
                .baseUrl(API_BASE_URL)
                .exchangeStrategies(exchangeStrategies)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();
    }
}
