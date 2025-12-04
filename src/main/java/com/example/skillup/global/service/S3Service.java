package com.example.skillup.global.service;


import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.exception.S3UploadException;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");

    //추후에 변경 가능 현재 10MB 로 설정
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Template s3Template;

    public String uploadFile(MultipartFile file, String dirName) {

        if (file == null || file.isEmpty()) {
            throw new S3UploadException(CommonErrorCode.FILE_UPLOAD_ERROR, "업로드할 파일이 비어있습니다.");
        }

        validateFileSize(file);

        String originalFileName = file.getOriginalFilename();

        validateFileExtension(originalFileName);

        String uuidFileName = UUID.randomUUID() + "_" + originalFileName;
        String key = dirName + "/" + uuidFileName;

        try (InputStream inputStream = file.getInputStream()) {

            S3Resource s3Resource = s3Template.upload(bucket, key, inputStream);
            return s3Resource.getURL().toString();

        } catch (IOException e) {
            log.error("파일 입력 스트림 에러: {}", e.getMessage());
            throw new S3UploadException(CommonErrorCode.FILE_READ_ERROR, e.getMessage());

        } catch (S3Exception e) {
            log.error("AWS S3 업로드 에러: {}", e.getMessage());
            throw new S3UploadException(CommonErrorCode.AWS_S3_SERVICE_ERROR, e.getMessage());

        } catch (Exception e) {
            log.error("알 수 없는 S3 업로드 에러: {}", e.getMessage());
            throw new S3UploadException(CommonErrorCode.FILE_UPLOAD_ERROR, e.getMessage());
        }
    }

    public void deleteFileFromUrl(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        deleteFile(key);
    }

    private void deleteFile(String key) {
        if (key == null || key.isBlank()) {
            throw new S3UploadException(CommonErrorCode.FILE_DELETE_ERROR, "삭제할 파일의 key 값이 존재하지 않습니다.");
        }
        try {
            String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);

            s3Template.deleteObject(bucket, decodedKey);
            log.info("S3 파일 삭제 성공: {}", decodedKey);

        } catch (S3Exception e) {
            log.error("AWS S3 파일 삭제 실패: {}", e.getMessage());
            throw new S3UploadException(CommonErrorCode.AWS_S3_SERVICE_ERROR, "S3 파일 삭제 중 오류가 발생했습니다.");

        } catch (Exception e) {
            log.error("알 수 없는 파일 삭제 에러: {}", e.getMessage());
            throw new S3UploadException(CommonErrorCode.FILE_DELETE_ERROR, e.getMessage());
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            return path.substring(1);

        } catch (MalformedURLException e) {
            log.error("URL 파싱 에러: {}", fileUrl);
            throw new S3UploadException(CommonErrorCode.FILE_READ_ERROR, e.getMessage());
        }
    }

    private void validateFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");

        if (lastDotIndex == -1) {
            throw new S3UploadException(CommonErrorCode.FILE_UPLOAD_ERROR, "파일 확장자가 없습니다.");
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new S3UploadException(CommonErrorCode.INVALID_FILE_TYPE, extension + "는 ");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new S3UploadException(CommonErrorCode.FILE_SIZE_EXCEED, "파일의 크기가 10MB 이상입니다.");
        }
    }
}
