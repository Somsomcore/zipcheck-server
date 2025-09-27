package somsomcore.zipcheck.domain.s3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import somsomcore.zipcheck.domain.s3.service.FileService;
import somsomcore.zipcheck.domain.s3.service.S3Service;
import somsomcore.zipcheck.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdfs")
public class S3Controller {

    private final S3Service storage;
    private final FileService fileService;

    // 업로드
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String key = storage.uploadPdf(file);  // 파일 업로드를 위한 호출 함수
        return ResponseEntity.ok(key);
    }

    // 다운로드
    @Operation(summary = "파일 다운로드", description = "관리자가 접수된 신고글 근거 자료를 key를 이용하여 다운로드합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @GetMapping("/download")
    public void download(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String key, HttpServletResponse res) throws Exception {

        fileService.downloadPdf(userDetails.getUser().getId(), key, res);
    }

    // 삭제
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String key) {
        storage.deletePdf(key);   // 파일 삭제를 위한 호출 함수
        return ResponseEntity.noContent().build();
    }

}