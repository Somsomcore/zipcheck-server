package somsomcore.zipcheck.domain.risk.dto.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ItemDto {
    // 🚨 XML 원본 태그 이름과 100% 일치하도록 수정!

    @JacksonXmlProperty(localName = "aptNm") // <aptNm>
    private String apartmentName;

    @JacksonXmlProperty(localName = "buildYear") // <buildYear>
    private int buildYear;

    @JacksonXmlProperty(localName = "contractTerm") // <contractTerm>
    private String contractTerm;

    @JacksonXmlProperty(localName = "contractType") // <contractType>
    private String contractType;

    @JacksonXmlProperty(localName = "dealDay") // <dealDay>
    private int day;

    @JacksonXmlProperty(localName = "dealMonth") // <dealMonth>
    private int month;

    @JacksonXmlProperty(localName = "dealYear") // <dealYear>
    private int year;

    @JacksonXmlProperty(localName = "deposit") // <deposit> (예: "1,000" 또는 "99,000")
    private String depositAmount; // String으로 받고 Service에서 파싱

    @JacksonXmlProperty(localName = "excluUseAr") // <excluUseAr>
    private Double dedicatedArea; // 전용면적

    @JacksonXmlProperty(localName = "floor") // <floor>
    private int floor;

    @JacksonXmlProperty(localName = "jibun") // <jibun>
    private String jibun;

    @JacksonXmlProperty(localName = "monthlyRent") // <monthlyRent> (예: "150" 또는 "0")
    private String monthlyRentAmount; // String으로 받고 Service에서 파싱

    @JacksonXmlProperty(localName = "preDeposit") // <preDeposit>
    private String previousDeposit;

    @JacksonXmlProperty(localName = "preMonthlyRent") // <preMonthlyRent>
    private String preMonthlyRent;

    @JacksonXmlProperty(localName = "sggCd") // <sggCd>
    private String regionCode; // 지역코드 (sggCd가 11110)

    @JacksonXmlProperty(localName = "umdNm") // <umdNm>
    private String legalDong; // 법정동 (umdNm이 효제동, 평동 등)

    @JacksonXmlProperty(localName = "useRRRight") // <useRRRight>
    private String renewalRightUsed;

    // 2. 계약면적 (단독/다가구/연립용)
    @JacksonXmlProperty(localName = "totalFloorAr")
    private Double totalFloorAr; // (totalFloorAr)

    // 3. 주택 유형 (단독/다가구/연립 구분용)
    @JacksonXmlProperty(localName = "houseType")
    private String houseType;
}