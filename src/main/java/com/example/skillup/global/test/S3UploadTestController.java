package com.example.skillup.global.test;

import com.example.skillup.global.common.BaseResponse;
import com.example.skillup.global.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class S3UploadTestController {
    private final S3UploaderService s3UploaderService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file) {
        return BaseResponse.success("성공적으로 이미지가 올라갔습니다.", s3UploaderService.uploadFile(file, "test"));
    }
}
