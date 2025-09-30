package com.example.skillup.domain.oauth.component;

import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Component
public class HttpClientHelper {

    public String get(String url, Map<String, String> headers) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                return readResponse(con.getInputStream());
            } else {
                String errorResponse = readResponse(con.getErrorStream());
                throw new OauthException(OauthErrorCode.FAIL_GET_USER_INFO,
                        "API 요청 실패. 응답 코드: " + responseCode + ", 메시지: " + errorResponse);
            }
        } catch (IOException e) {
            throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
