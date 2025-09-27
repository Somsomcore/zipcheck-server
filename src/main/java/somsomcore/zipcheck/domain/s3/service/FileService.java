package somsomcore.zipcheck.domain.s3.service;

import com.amazonaws.services.s3.model.S3Object;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.domain.user.repository.UserRepository;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.handler.UserHandler;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FileService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public void downloadPdf(Long userId, String key, HttpServletResponse res) throws Exception {
        // 1. 사용자 조회 및 ADMIN 권한 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        if (user.getRole() != Role.ADMIN) {
            throw new UserHandler(ErrorStatus.USER_FORBIDDEN);
        }

        // 2. S3에서 파일 객체 다운로드
        try (S3Object s3obj = s3Service.downloadPdf(key);
             InputStream in = s3obj.getObjectContent()) {

            // 3. 파일 이름 가공 로직
            String originalName = extractOriginalName(key);

            // 4. HTTP 응답 헤더 설정
            setAttachmentHeaders(res, originalName, s3obj.getObjectMetadata().getContentLength());

            // 5. 파일 스트림 복사 및 응답
            IOUtils.copy(in, res.getOutputStream());
            res.flushBuffer();
        }
    }

    private String extractOriginalName(String key) {
        String filenameWithUuid = key.substring(key.lastIndexOf('/') + 1);
        String originalName = filenameWithUuid.replaceFirst("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-", "");
        if (!originalName.toLowerCase().endsWith(".pdf")) {
            originalName += ".pdf";
        }
        return originalName;
    }

    private void setAttachmentHeaders(HttpServletResponse res, String originalName, long contentLength) throws Exception {
        String encodedRFC5987 = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String asciiFallback = originalName.replaceAll("[^\\x00-\\x7F]", "_");

        res.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encodedRFC5987);
        res.setContentType("application/pdf");
        if (contentLength > 0) {
            res.setContentLengthLong(contentLength);
        }
    }
}