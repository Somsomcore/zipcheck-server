package somsomcore.zipcheck.domain.report.repository;

public interface ReportAddressCount {
    Double getLatitude();
    Double getLongitude();
    String getAddress();
    Integer getReportCount();
}
