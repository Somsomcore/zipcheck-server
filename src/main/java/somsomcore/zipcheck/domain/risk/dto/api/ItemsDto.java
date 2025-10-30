package somsomcore.zipcheck.domain.risk.dto.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Collections; // 빈 리스트 처리를 위해 추가
import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class ItemsDto {

    // <items><item>...</item><item>...</item></items> 구조 매핑
    // 만약 <items/> 처럼 비어있을 때 item 필드가 null이 될 수 있으므로, null 대신 빈 리스트 반환 처리 추가
    @JacksonXmlElementWrapper(useWrapping = false) // <items> 태그 자체는 무시
    @JacksonXmlProperty(localName = "item")
    private List<ItemDto> item;

    // item이 null일 경우 빈 리스트 반환 (NullPointerException 방지)
    public List<ItemDto> getItem() {
        return item == null ? Collections.emptyList() : item;
    }
}
