package com.example.skillup.domain.healthCheck;

import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @Operation(
            summary = "헬스 체크 API",
            description = "Alertmanager가 서버의 생존 여부를 확인하기 위해 호출하는 간단한 요청입니다."
    )
    @GetMapping("/health")
    public BaseResponse<Void> healthCheck() {
        return BaseResponse.success("헬스 체크 성공", null);
    }
}
