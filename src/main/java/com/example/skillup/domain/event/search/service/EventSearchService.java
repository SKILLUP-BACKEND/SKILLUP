package com.example.skillup.domain.event.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.search.document.EventDocument;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EventMapper eventMapper;

    public EventResponse.SearchEventResponseList search(EventRequest.EventSearchRequest request) throws IOException {
        // ---- 전처리 ----
        String raw = request.getSearchString() == null ? "" : request.getSearchString().trim();
        String q = raw.toLowerCase();
        String qNoSpace = q.replaceAll("\\s+", "");
        if (q.length() < 2) {
            return EventResponse.SearchEventResponseList.builder()
                    .total(0).homeEventResponseList(List.of()).message("query-too-short(>=2)").build();
        }
        int page = request.getPage() == null ? 0 : Math.max(0, request.getPage());
        int size = request.getSize() == null ? 20 : Math.max(1, Math.min(request.getSize(), 50));
        String sortKey = request.getSort() == null ? "recommended" : request.getSort();

        // ---- 공통 bool ----
        BoolQuery.Builder bool = new BoolQuery.Builder()
                .must(m -> m.term(t -> t.field("status").value("published")))
                .filter(f -> f.range(r -> r.field("recruit_end").gte(JsonData.of("now"))))
                .should(s -> s.multiMatch(mm -> mm
                        .query(q)
                        .fields("title^5","title.ngram^3","description^2")
                        .operator(Operator.And)))
                .should(s -> s.term(t -> t.field("title.nospace").value(qNoSpace).boost(3.0f)))
                .minimumShouldMatch("1");

        // ---- 필터 ----
        if (Boolean.TRUE.equals(request.getFreeOnly())) {
            bool.filter(f -> f.term(t -> t.field("is_free").value(true)));
        }
        if (request.getModes() != null && !request.getModes().isEmpty()) {
            boolean hasOnline = request.getModes().stream().anyMatch("ONLINE"::equalsIgnoreCase);
            boolean hasOffline = request.getModes().stream().anyMatch("OFFLINE"::equalsIgnoreCase);
            if (hasOnline ^ hasOffline) { // 한쪽만 선택된 경우
                bool.filter(f -> f.term(t -> t.field("is_online").value(hasOnline)));
            }
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            bool.filter(f -> f.term(t -> t.field("location_text").value(request.getCity().toLowerCase())));
        }

        Query query = new Query.Builder()
                .bool(bool.build())
                .build();

        // ---- 정렬/점수 ----
//        Query query;
//        List<SortOptions> sort = new ArrayList<>();
//        switch (sortKey) {
//            case "deadline" -> {
//                query = QueryBuilders.bool(bool.build())._toQuery();
//                sort.add(SortOptions.of(s -> s.field(f -> f.field("recruit_end").order(SortOrder.Asc))));
//                sort.add(SortOptions.of(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc))));
//            }
//            case "created" -> {
//                query = QueryBuilders.bool(bool.build())._toQuery();
//                sort.add(SortOptions.of(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc))));
//            }
//            default -> { // recommended
//                query = QueryBuilders.functionScore(fs -> fs
//                        .query(qb -> qb.bool(bool.build()))
//                        .functions(f -> f.fieldValueFactor(fvf -> fvf.field("popularity_score").factor(1.0)))
//                        .functions(f -> f.gauss(g -> g.field("created_at").scale("14d").decay(0.5).weight(2.0f)))
//                        .scoreMode(FunctionScoreMode.Sum)
//                        .boostMode(FunctionBoostMode.Sum)
//                )._toQuery();
//            }
//        }

        // ---- 메인 검색 ----
//        SearchResponse<EventDocument> res = elasticsearchClient.search(s -> s
//                        .index("events_v1")
//                        .from(page * size).size(size)
//                        .query(query)
//                        .highlight(h -> h
//                                .fields("title", f -> f)
//                                .fields("description", f -> f)
//                                .preTags("<em>").postTags("</em>"))
//                        .sort(sort),
//                EventDocument.class);

        SearchResponse<EventDocument> res = elasticsearchClient.search(s -> s
                        .index("events_v1")
                        .from(page * size).size(size)
                        .query(query)
                        .highlight(h -> h
                                .fields("title", f -> f)
                                .fields("description", f -> f)
                                .preTags("<em>").postTags("</em>")),
                EventDocument.class);

        int total = res.hits().total() == null ? 0 : (int) res.hits().total().value();
        if (total > 0) {
            var items = res.hits().hits().stream()
                    .map(hit -> eventMapper.mapEsDocToHomeItem(hit.source(), hit.highlight(), hit.score()))
                    .toList();
            return EventResponse.SearchEventResponseList.builder()
                    .total(total).homeEventResponseList(items).message("ok").build();
        }

        // ---- 폴백 1: 해시태그 추천 ----
        SearchResponse<EventDocument> byTag = elasticsearchClient.search(s -> s
                        .index("events_v1").size(10)
                        .query(qb -> qb.bool(b -> b
                                .filter(f -> f.range(r -> r.field("recruit_end").gte(JsonData.of("now"))))
                                .must(m -> m.terms(ts -> ts.field("hashtags")
                                        .terms(v -> v.value(List.of(FieldValue.of(q.toLowerCase())))))))
                        )
                        .sort(o -> o.field(f -> f.field("popularity_score").order(SortOrder.Desc))),
                EventDocument.class);
        if (!byTag.hits().hits().isEmpty()) {
            var items = byTag.hits().hits().stream()
                    .map(hit -> eventMapper.mapEsDocToHomeItem(hit.source(), hit.highlight(), hit.score()))
                    .toList();
            return EventResponse.SearchEventResponseList.builder()
                    .total(items.size()).homeEventResponseList(items)
                    .message("no-results: hashtag recommendations").build();
        }

        // ---- 폴백 2: 인기 Top-N ----
        SearchResponse<EventDocument> top = elasticsearchClient.search(s -> s
                        .index("events_v1").size(10)
                        .query(qb -> qb.range(r -> r.field("recruit_end").gte(JsonData.of("now"))))
                        .sort(o -> o.field(f -> f.field("popularity_score").order(SortOrder.Desc))),
                EventDocument.class);

        var items = top.hits().hits().stream()
                .map(hit -> eventMapper.mapEsDocToHomeItem(hit.source(), hit.highlight(), hit.score()))
                .toList();

        return EventResponse.SearchEventResponseList.builder()
                .total(items.size()).homeEventResponseList(items)
                .message("no-results: top popularity").build();
    }
}
