package somsomcore.zipcheck.domain.risk.dto.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class BodyDto {

    @JacksonXmlProperty(localName = "items")
    private ItemsDto items; // 실제 데이터 목록 담는 객체

    @JacksonXmlProperty(localName = "numOfRows")
    private int numOfRows; // 페이지당 결과 수

    @JacksonXmlProperty(localName = "pageNo")
    private int pageNo; // 현재 페이지 번호

    @JacksonXmlProperty(localName = "totalCount")
    private int totalCount; // 전체 결과 수 (페이지 계산에 중요!)
}
