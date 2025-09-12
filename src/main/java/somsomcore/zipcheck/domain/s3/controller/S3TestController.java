package somsomcore.zipcheck.domain.s3.controller;

import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.S3Object;

import jakarta.servlet.http.HttpServletResponse;
import somsomcore.zipcheck.domain.s3.service.S3Service;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdfs")
public class S3TestController {

    private final S3Service storage;

    // 업로드
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String key = storage.uploadPdf(file);  // 파일 업로드를 위한 호출 함수
        return ResponseEntity.ok(key);
    }

    // 다운로드
    @GetMapping("/download")
    public void download(@RequestParam String key, HttpServletResponse res) throws Exception {
        try (S3Object s3obj = storage.downloadPdf(key);  // 파일 삭제를 위한 호출 함수
             InputStream in = s3obj.getObjectContent()) {

            // 1) 키에서 파일명 부분만 추출
            String filenameWithUuid = key.substring(key.lastIndexOf('/') + 1); // "UUID-원본명.pdf"
            // 2) 맨 앞의 UUID- 패턴만 제거 (36자 UUID + 하이픈)
            String originalName = filenameWithUuid.replaceFirst("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-", "");
            // 3) 확장자 보정 (혹시 원본명이 확장자 빠졌다면)
            if (!originalName.toLowerCase().endsWith(".pdf")) {
                originalName += ".pdf";
            }

            // 브라우저 호환을 위한 Content-Disposition (filename + filename*)
            String encodedRFC5987 = java.net.URLEncoder.encode(originalName, java.nio.charset.StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            String asciiFallback = toAsciiFallback(originalName); // 아래 헬퍼

            res.setHeader("Content-Disposition",
                    "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encodedRFC5987);
            res.setContentType("application/pdf");

            // (선택) Content-Length 설정: 있으면 다운로드 UX가 좋아짐
            long len = s3obj.getObjectMetadata().getContentLength();
            if (len > 0) res.setContentLengthLong(len);

            IOUtils.copy(in, res.getOutputStream());
            res.flushBuffer();
        }
    }

    // 삭제
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String key) {
        storage.deletePdf(key);   // 파일 삭제를 위한 호출 함수
        return ResponseEntity.noContent().build();
    }

    private String toAsciiFallback(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c <= 0x7F) sb.append(c);
            else sb.append('_'); // 비ASCII는 언더스코어로 대체
        }
        return sb.toString();
    }
}