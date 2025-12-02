package com.example.skillup.global.service;

import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.exception.S3UploadException;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
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
public class S3UploaderService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Template s3Template;

    public String uploadFile(MultipartFile file, String dirName) {

        if (file.isEmpty()) {
            throw new S3UploadException(CommonErrorCode.FILE_UPLOAD_ERROR, "업로드할 파일이 비어있습니다.");
        }

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

    private void validateFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");

        if (lastDotIndex == -1) {
            throw new S3UploadException(CommonErrorCode.FILE_UPLOAD_ERROR, "파일 확장자가 없습니다.");
        }


        String extension = filename.substring(lastDotIndex + 1).toLowerCase();


        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new S3UploadException(CommonErrorCode.INVALID_FILE_TYPE, extension +"는 ");
        }
    }
}
