package somsomcore.zipcheck.domain.risk.dto.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString; // 디버깅 시 유용

@Getter
@NoArgsConstructor
@ToString // 객체 내용 쉽게 보려고 추가
@JacksonXmlRootElement(localName = "response") // <response> 태그 매핑
public class ApiResponseDto {

    @JacksonXmlProperty(localName = "header")
    private HeaderDto header;

    @JacksonXmlProperty(localName = "body")
    private BodyDto body;
}

