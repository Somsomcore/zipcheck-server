package somsomcore.zipcheck.domain.risk.dto.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class HeaderDto {

    @JacksonXmlProperty(localName = "resultCode")
    private String resultCode; // 예: "00" (성공)

    @JacksonXmlProperty(localName = "resultMsg")
    private String resultMsg; // 예: "NORMAL SERVICE."
}