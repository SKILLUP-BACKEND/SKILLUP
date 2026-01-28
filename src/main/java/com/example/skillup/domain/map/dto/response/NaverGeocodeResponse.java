package com.example.skillup.domain.map.dto.response;

import java.util.List;
import lombok.Getter;

@Getter
public class NaverGeocodeResponse {
    private List<Address> addresses;

    @Getter
    public static class Address {
        private String x; // 경도(lng)
        private String y; // 위도(lat)
        private String roadAddress;
        private String jibunAddress;
    }
}
