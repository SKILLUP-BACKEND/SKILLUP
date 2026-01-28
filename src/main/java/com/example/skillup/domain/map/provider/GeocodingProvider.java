package com.example.skillup.domain.map.provider;

import com.example.skillup.domain.map.entity.ProviderType;

public interface GeocodingProvider {

    ProviderType getType();
    GeoPoint geocode(String query);

    record GeoPoint(double lat, double lng , String roadAddress) {}
}
