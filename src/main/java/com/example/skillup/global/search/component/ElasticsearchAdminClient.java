package com.example.skillup.global.search.component;

import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.search.dto.SynonymsSetRequest;
import com.example.skillup.global.search.exception.SearchException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchAdminClient {

    private final org.elasticsearch.client.RestClient low;
    private final ObjectMapper objectMapper;

    public String upsertSynonymsSet(String setId, SynonymsSetRequest requestBody) {
        try {
            Request req = new Request("PUT", "/_synonyms/" + setId);
            req.setJsonEntity(objectMapper.writeValueAsString(requestBody));

            Response res = low.performRequest(req);

            return EntityUtils.toString(res.getEntity());
        } catch (Exception e) {
            throw new SearchException(
                    CommonErrorCode.ELASTICSEARCH_ERROR,
                    "ES synonyms set 업데이트 실패(setId=" + setId + "): " + e.getMessage()
            );
        }
    }

}
