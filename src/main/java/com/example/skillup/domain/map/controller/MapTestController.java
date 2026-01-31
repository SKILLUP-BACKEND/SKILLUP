package com.example.skillup.domain.map.controller;

import com.example.skillup.domain.map.provider.GeocodingProvider;
import com.example.skillup.domain.map.service.GeocodingService;
import com.example.skillup.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class MapTestController {

    private final GeocodingService geocodingService;

    @GetMapping("/geocode")
    public BaseResponse<GeocodingProvider.GeoPoint> geocode(@RequestParam String query) {
        return BaseResponse.success("좌표 변환에 성공했습니다.",geocodingService.geocode(query));
    }
}
