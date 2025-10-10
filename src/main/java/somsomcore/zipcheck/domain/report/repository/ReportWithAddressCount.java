package somsomcore.zipcheck.domain.report.repository;

public interface ReportWithAddressCount {
    Long getReportId();
    String getAddr();
    String getAddrDetail();
    Long getClassificationId();
    Long getContractTypeId();
    Long getCount();
}
