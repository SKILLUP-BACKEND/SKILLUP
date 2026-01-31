package com.example.skillup.domain.map.provider;

import com.example.skillup.domain.map.dto.response.NaverGeocodeResponse;
import com.example.skillup.domain.map.entity.ProviderType;
import com.example.skillup.domain.map.exception.MapErrorCode;
import com.example.skillup.domain.map.exception.MapException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class NaverGeocodingProvider implements GeocodingProvider {

    private final WebClient naverMapWebClient;

    @Value("${map.naver.geocode-path}")
    private String geocodePath;

    @Override
    public ProviderType getType() {
        return ProviderType.NAVER;
    }

    @Override
    public GeoPoint geocode(String query) {
        if (query == null || query.isBlank()) {
            throw new MapException(MapErrorCode.INVALID_ADDRESS_QUERY, "query(주소)는 필수입니다.");
        }

        NaverGeocodeResponse res = naverMapWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(geocodePath)
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        r -> r.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new MapException(
                                        MapErrorCode.MAP_API_CLIENT_ERROR,
                                        "NAVER_GEOCODE_4XX: " + body
                                ))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        r -> r.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new MapException(
                                        MapErrorCode.MAP_API_SERVER_ERROR,
                                        "NAVER_GEOCODE_5XX: " + body
                                ))
                )
                .bodyToMono(NaverGeocodeResponse.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorMap(e -> (e instanceof MapException) ? e :
                        new MapException(MapErrorCode.MAP_GEOCODE_ERROR,
                                "NAVER_GEOCODE_CALL_FAILED: " + e.getMessage()))
                .block();

        if (res == null || res.getAddresses() == null || res.getAddresses().isEmpty()) {
            throw new MapException(MapErrorCode.COORDINATE_NOT_FOUND, "주소에 해당하는 좌표를 찾지 못했습니다.");
        }

        var primaryAddress = res.getAddresses().get(0);
        if (!StringUtils.hasText(primaryAddress.getX()) || !StringUtils.hasText(primaryAddress.getY())) {
            throw new MapException(MapErrorCode.MAP_API_RESPONSE_PARSE_ERROR, "NAVER_GEOCODE_PARSE_ERROR");
        }

        // 네이버: x=경도(lng), y=위도(lat)
        return new GeoPoint(Double.parseDouble(primaryAddress.getY()), Double.parseDouble(primaryAddress.getX()) , primaryAddress.getRoadAddress());
    }

}
