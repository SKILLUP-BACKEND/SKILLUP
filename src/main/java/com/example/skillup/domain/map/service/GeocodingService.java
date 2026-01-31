package com.example.skillup.domain.map.service;

import com.example.skillup.domain.map.provider.GeocodingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final GeocodingProvider naverProvider;

    public GeocodingProvider.GeoPoint geocode(String query) {
        return naverProvider.geocode(query);
    }
}
