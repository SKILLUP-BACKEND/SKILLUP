package com.example.skillup.domain.event.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.search.document.EventDocument;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EventMapper eventMapper;

    public EventResponse.SearchEventResponseList search(EventRequest.EventSearchRequest request) {
        // 1) 전처리

        String searchString = request.getSearchString() == null ? "" : request.getSearchString().trim();
        String searchStringNoSpace = searchString.toLowerCase().replaceAll("\\s+", "");

        System.out.println(searchStringNoSpace);

        if (searchStringNoSpace.length() < 2) {
            throw new EventException(EventErrorCode.EVENT_SEARCH_QUERY_TOO_SHORT, "검색어는 2글자 이상 입력해주세요");
        }

        Integer size = 12;

        // 2) 검색 실행 (제목 전용)
        SearchResponse<EventDocument> documentSearchResponse;
        try {
            documentSearchResponse = elasticsearchClient.search(s -> s
                            .index("events_v1")
                            .from(request.getPage() * size)
                            .size(size)
                            .query(qb -> qb.bool(b -> b
                                    .should(sh -> sh.multiMatch(mm -> mm
                                            .query(searchString)
                                            .fields("title^5", "title.ngram^3")
                                            .operator(Operator.And)))
                                    .should(sh -> sh.term(t -> t.field("title.nospace").value(searchStringNoSpace).boost(3.0f)))
                                    .should(sh -> sh.match(m -> m
                                            .field("title.nospace_infix").query(searchStringNoSpace).boost(3.0f)
                                    ))
                                    .should(sh -> sh.prefix(
                                            p -> p.field("title.nospace").value(searchStringNoSpace).boost(3.0f)))
                                    .minimumShouldMatch("1")
                            ))
                            .highlight(h -> h
                                    .fields("title", f -> f
                                            .matchedFields("title","title.ngram","title.nospace")
                                            .requireFieldMatch(false)
                                            .preTags("<em>").postTags("</em>")
                                            .numberOfFragments(0)
                                    )
                            )
                    , EventDocument.class);
        } catch (IOException e) {
            throw new EventException(EventErrorCode.EVENT_SEARCH_ERROR, e.getMessage());
        }

        // 3) 결과 매핑
        int total =
                documentSearchResponse.hits().total() == null ? 0 : (int) documentSearchResponse.hits().total().value();
        var items = documentSearchResponse.hits().hits().stream()
                .map(hit -> eventMapper.mapEsDocToHomeItem(hit.source(), hit.highlight(), hit.score()))
                .toList();
    //TODO : 필터 기능 추가
        return eventMapper.toSearchEventResponseList(total, items);
    }
}

