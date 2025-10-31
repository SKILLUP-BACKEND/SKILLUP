package com.example.skillup.global.search.component;

import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.search.exception.SearchException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchAdminClient {

    private final org.elasticsearch.client.RestClient low;

    @Value("${skillup.search.index-name}")
    private String index;

    public String reloadSearchAnalyzers() {
        try {
            var req = new org.elasticsearch.client.Request("POST", "/" + index + "/_reload_search_analyzers");
            var res = low.performRequest(req);
            return org.apache.http.util.EntityUtils.toString(res.getEntity());
        } catch (Exception e) {
            throw new SearchException(CommonErrorCode.FILE_UPLOAD_ERROR, "ES 분석기 재로드 실패: " + e.getMessage());
        }
    }

}
