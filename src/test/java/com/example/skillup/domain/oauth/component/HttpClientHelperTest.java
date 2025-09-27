package com.example.skillup.domain.oauth.component;

import com.example.skillup.domain.oauth.exception.OauthException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HttpClientHelperTest {

    @Autowired
    private HttpClientHelper httpClientHelper;

    @Test
    void testGet_Success() {
        String url = "https://jsonplaceholder.typicode.com/todos/1";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String response = httpClientHelper.get(url, headers);
        assertNotNull(response);
        assertTrue(response.contains("\"id\": 1"));
    }

    @Test
    void testGet_Fail() {
        String url = "https://jsonplaceholder.typicode.com/invalid-url";

        OauthException exception = assertThrows(OauthException.class,
                () -> httpClientHelper.get(url, null));

        assertTrue(exception.getMessage().contains("API 요청 실패"));
    }
}