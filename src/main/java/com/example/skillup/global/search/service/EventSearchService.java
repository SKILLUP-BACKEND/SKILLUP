package com.example.skillup.global.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventFormat;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.search.document.EventDocument;
import com.example.skillup.global.search.exception.SearchErrorCode;
import com.example.skillup.global.search.exception.SearchException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventSearchService {

    @Value("${skillup.search.index-name}")
    private String index;

    private final ElasticsearchClient elasticsearchClient;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;

    LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();

    public EventResponse.SearchEventResponseList search(EventRequest.EventSearchRequest request) {

        // 1) 전처리
        String searchString = request.getSearchString() == null ? "" : request.getSearchString().trim();
        String searchStringNoSpace = searchString.toLowerCase().replaceAll("\\s+", "");

        System.out.println(searchStringNoSpace);

        if (searchStringNoSpace.length() < 2) {
            throw new EventException(EventErrorCode.EVENT_SEARCH_QUERY_TOO_SHORT, "검색어는 2글자 이상 입력해주세요");
        }

        final Integer size = 12;

        BoolQuery.Builder bool = new BoolQuery.Builder()
                .must(m -> m.bool(b -> b
                                .should(s -> s.match(mm -> mm
                                        .field("title")
                                        .query(searchString)
                                        .analyzer("ko_query_syn")
                                        .operator(Operator.And)
                                        .boost(3.0f)
                                ))
                                .should(s -> s.multiMatch(mm -> mm
                                        .query(searchString)
                                        .fields("title.ngram^1.8")
                                        .analyzer("ko_query_syn")
                                        .type(TextQueryType.MostFields)
                                ))
                                .should(s -> s.match(mm -> mm
                                        .field("title.nospace_infix")
                                        .query(searchStringNoSpace)
                                        .boost(1.5f)
                                ))
                                .minimumShouldMatch("1")
                                .should(sh -> sh.term(t -> t.field("title.nospace").value(searchStringNoSpace).boost(3.0f)))
                                .should(sh -> sh.prefix(p -> p.field("title.nospace").value(searchStringNoSpace).boost(3.0f)))
                        )
                );

        // 무료 필터
        if (Boolean.TRUE.equals(request.getIsFree())) {
            bool.filter(f -> f.term(t -> t.field("is_free").value(true)));
        }

        // 온라인/오프라인 필터
        EventFormat fmt = request.getEventFormat();
        if (fmt == EventFormat.ONLINE) {
            bool.filter(f -> f.term(t -> t.field("is_online").value(true)));
        } else if (fmt == EventFormat.OFFLINE) {
            bool.filter(f -> f.term(t -> t.field("is_online").value(false)));
        }

        // 기간 필터
        if (request.getEventStart() != null) {
            bool.filter(f -> f.range(r -> r.field("event_start").gte(JsonData.of(request.getEventStart().toString()))));
        }
        if (request.getEventEnd() != null) {
            bool.filter(f -> f.range(r -> r.field("event_end").lte(JsonData.of(request.getEventEnd().toString()))));
        }

        Query query = new Query.Builder().bool(bool.build()).build();

        String sortKey = request.getSort() == null ? "POPULARITY" : request.getSort().toString();
        List<SortOptions> sorts = new ArrayList<>();
        switch (sortKey) {
            case "DEADLINE" -> {
                // 마감 임박 오름차순 + 같은 날이면 최신 등록 우선
                sorts.add(SortOptions.of(s -> s.field(f -> f.field("recruit_end").order(SortOrder.Asc))));
                sorts.add(SortOptions.of(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc))));
            }
            case "LATEST" -> {
                sorts.add(SortOptions.of(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc))));
            }
            default -> { // POPULARITY
                sorts.add(SortOptions.of(s -> s.field(f -> f.field("popularity_score").order(SortOrder.Desc))));
                sorts.add(SortOptions.of(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc))));
            }
        }

        // 2) 검색 실행
        SearchResponse<EventDocument> documentSearchResponse;
        try {
            documentSearchResponse = elasticsearchClient.search(s -> s
                            .index(index)
                            .from(request.getPage() * size)
                            .size(size)
                            .query(query)
                            .sort(sorts)
                    , EventDocument.class);
        } catch (IOException e) {
            throw new SearchException(SearchErrorCode.SEARCH_SEARCH_ERROR, e.getMessage());
        }

        // 3) 결과 매핑
        int total =
                documentSearchResponse.hits().total() == null ? 0 : (int) documentSearchResponse.hits().total().value();

        if (total == 0) {
            var rows = eventRepository.findPopularForHomeWithPopularity(
                    null,
                    since,
                    LocalDateTime.now(),
                    PageRequest.of(0, 10)
            );

            return eventMapper.toSearchEventResponseList(rows.size(), rows.stream()
                    .map(r -> {
                        double score = r.getPopularity();
                        Event event = r.getEvent();
                        boolean recommended = event.isRecommendedManual();
                        return eventMapper.toFeaturedEvent(event, false, recommended, event.isAd(), score);
                    }).toList());
        }
        List<EventResponse.HomeEventResponse> items = documentSearchResponse.hits().hits().stream()
                .map(hit -> eventMapper.mapEsDocToHomeItem(hit.source(), hit.score()))
                .toList();

        return eventMapper.toSearchEventResponseList(total, items);
    }
}

