package somsomcore.zipcheck.domain.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;
import somsomcore.zipcheck.global.apiPayload.exception.handler.S3Handler;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 하위 폴더
    private static final String PREFIX = "document/";

    // 업로드
    public String uploadPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new S3Handler(ErrorStatus.FILE_EMPTY);
        }

        // PDF MIME 체크
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("pdf")) {
            throw new S3Handler(ErrorStatus.FILE_NOT_PDF);
        }

        // 파일명 설계: document/{yyyy}/{MM}/UUID-원본이름.pdf 등으로 확장 가능
        String key = PREFIX + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("application/pdf");
        meta.setContentLength(file.getSize());

        try (InputStream is = file.getInputStream()) {
            PutObjectRequest req = new PutObjectRequest(bucketName, key, is, meta);
            amazonS3.putObject(req);
        } catch (Exception e) {
            throw new S3Handler(ErrorStatus.FILE_UPLOAD_FAILED);
        }

        return key; // 저장된 오브젝트 키를 반환
    }

    // 다운로드
    public S3Object downloadPdf(String key) {
        // 키는 반드시 "document/"로 시작하게끔 검증
        if (!key.startsWith(PREFIX)) throw new S3Handler(ErrorStatus.FILE_INVALID_PATH);
        try {
            return amazonS3.getObject(bucketName, key);
        } catch (Exception e) {
            throw new S3Handler(ErrorStatus.FILE_NOT_FOUND);
        }
    }

    // 삭제
    public void deletePdf(String key) {
        if (!key.startsWith(PREFIX)) {
            throw new S3Handler(ErrorStatus.FILE_INVALID_PATH);
        }
        try {
            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new S3Handler(ErrorStatus.FILE_DELETE_FAILED);
        }
    }

    private String sanitize(String name) {
        if (name == null) return "file.pdf";
        return name.replaceAll("[\\\\/]", "_").replaceAll("\\s+", "_");
    }

}