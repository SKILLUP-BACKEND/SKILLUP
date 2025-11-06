package com.example.skillup.domain.admin.controller;

import com.example.skillup.domain.admin.dto.AdminLoginRequest;
import com.example.skillup.domain.admin.dto.SynonymRequest;
import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.service.AdminService;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import com.example.skillup.global.search.service.EventIndexerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;
    private final AdminService adminService;
    private final EventIndexerService eventIndexerService;

    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(
            @RequestBody @Valid AdminLoginRequest request
    ) {
        Admin admin = adminService.login(request);

        TokenResponse tokenResponse = authService.login(null, admin.getRole().toString());

        return BaseResponse.success("관리자 로그인에 성공했습니다.", tokenResponse);
    }

    @PostMapping("/search/upload")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "초기 존재하는 행사 인덱싱 api", description = "ElasticSearch 초기 세팅시 존재하는 행사들을 추가하는 api")
    public BaseResponse<String> reindexAll() {
        long total = eventIndexerService.bulkIndexAll();
        return BaseResponse.success("현재 존재하는 행사 값들을 인덱싱 작업을 성공했습니다.", "변환된 행사의 개수 : " + total);
    }

    @PostMapping("/synonyms/publish")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "es에 DB 에 저장되어있는 동의어 내용을 연동하는 API", description = "")
    public BaseResponse<String> publish(
            @RequestParam(defaultValue = "ko") String locale) {
        return BaseResponse.success("동의어 사전 업로드 성공했습니다.", adminService.publish(locale));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "새로운 그룹을 추가하고 동의어를 추가하는 api 입니다.", description = "")
    public BaseResponse<String> createGroup(@RequestBody @Valid SynonymRequest.CreateSynonymRequest request) {
        return BaseResponse.success("성공적으로 생성되었습니다.", adminService.createSynonymGroupAndTerm(request));
    }

    //용어 추가
    @PostMapping("/groups/{groupId}/terms")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "존재하는 동의어 그룹에 용어를 추가하는 api", description = "")
    public BaseResponse<String> addTerms(@PathVariable Long groupId,
                                         @RequestBody @Valid SynonymRequest.AddTermsReq req) {
        return BaseResponse.success("성공적으로 추가되었습니다.", adminService.addTermsToSynonymGroup(groupId, req));
    }

    @DeleteMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "해당 동의어 그룹을 삭제합니다.")
    public BaseResponse<String> deleteGroup(@PathVariable Long groupId) {
        return BaseResponse.success("성공적으로 해당 그룹이 삭제되었습니다.", "지워진 동의어들 : " + adminService.deleteGroup(groupId));
    }
}
