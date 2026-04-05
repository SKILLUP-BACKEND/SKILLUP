package com.example.skillup.global.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.enums.EventSortType;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.service.EventService;
import com.example.skillup.global.search.document.EventDocument;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventSearchServiceTest {

    @InjectMocks
    private EventSearchService eventSearchService;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventService eventService;

    @Captor
    private ArgumentCaptor<java.util.function.Function> searchCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventSearchService, "index", "events_test");
    }

    // -- 헬퍼 --

    private EventDocument createDoc(Long id, String title, Double popularityScore) {
        return EventDocument.builder()
                .id(id)
                .title(title)
                .popularityScore(popularityScore)
                .category("BOOTCAMP_CLUB")
                .isFree(true)
                .isOnline(false)
                .thumbnailUrl("https://example.com/thumb.jpg")
                .createdAt(Instant.now())
                .eventStart(Instant.now())
                .eventEnd(Instant.now().plusSeconds(86400))
                .recruitStart(Instant.now())
                .recruitEnd(Instant.now().plusSeconds(86400))
                .recommendedManual(false)
                .ad(false)
                .build();
    }

    private EventRequest.EventSearchRequest buildRequest(String query, EventSortType sort) {
        return EventRequest.EventSearchRequest.builder()
                .searchString(query)
                .sort(sort)
                .page(0)
                .build();
    }

    @SuppressWarnings("unchecked")
    private SearchResponse<EventDocument> buildSearchResponse(List<Hit<EventDocument>> hits, int total) {
        TotalHits totalHits = TotalHits.of(t -> t.value(total).relation(TotalHitsRelation.Eq));
        HitsMetadata<EventDocument> hitsMetadata = HitsMetadata.of(h -> h.total(totalHits).hits(hits));
        return SearchResponse.of(s -> s
                .took(1)
                .timedOut(false)
                .shards(ShardStatistics.of(sh -> sh.total(1).successful(1).failed(0)))
                .hits(hitsMetadata)
        );
    }

    private Hit<EventDocument> buildHit(EventDocument doc, double score) {
        return Hit.of(h -> h
                .index("events_test")
                .id(doc.getId().toString())
                .score(score)
                .source(doc)
        );
    }

    // -- 테스트 --

    @Test
    @DisplayName("검색어가 2글자 미만이면 예외 발생")
    void search_throwsException_whenQueryTooShort() {
        EventRequest.EventSearchRequest request = buildRequest("a", EventSortType.POPULARITY);

        assertThatThrownBy(() -> eventSearchService.search(request, null))
                .isInstanceOf(EventException.class)
                .hasMessageContaining("검색어는 2글자 이상 입력해주세요");
    }

    @Test
    @DisplayName("POPULARITY 정렬 - 정확 일치(높은 _score)가 인기도 높은 부분 일치보다 먼저 반환된다")
    void search_popularity_exactMatchFirst() throws Exception {
        // given
        EventDocument exactMatch = createDoc(1L, "해커톤", 10.0);
        EventDocument partialMatch = createDoc(2L, "해커톤 대회 블라블라", 50.0);

        // 정확 일치 = score 높음, 부분 일치 = score 낮음
        Hit<EventDocument> hit1 = buildHit(exactMatch, 15.0);
        Hit<EventDocument> hit2 = buildHit(partialMatch, 5.0);

        SearchResponse<EventDocument> response = buildSearchResponse(List.of(hit1, hit2), 2);

        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(EventDocument.class)))
                .thenReturn(response);
        when(eventService.getBookmarkedEventId(any(), any())).thenReturn(Collections.emptySet());

        EventResponse.HomeEventResponse homeResp1 = EventResponse.HomeEventResponse.builder()
                .id(1L).build();
        EventResponse.HomeEventResponse homeResp2 = EventResponse.HomeEventResponse.builder()
                .id(2L).build();

        when(eventMapper.mapEsDocToHomeItem(eq(exactMatch), eq(15.0), eq(false))).thenReturn(homeResp1);
        when(eventMapper.mapEsDocToHomeItem(eq(partialMatch), eq(5.0), eq(false))).thenReturn(homeResp2);
        when(eventMapper.toSearchEventResponseList(eq(2), any(), eq(false)))
                .thenAnswer(inv -> {
                    List<EventResponse.HomeEventResponse> items = inv.getArgument(1);
                    return EventResponse.SearchEventResponseList.builder()
                            .total(2)
                            .homeEventResponseList(items)
                            .fallback(false)
                            .build();
                });

        EventRequest.EventSearchRequest request = buildRequest("해커톤", EventSortType.POPULARITY);

        // when
        EventResponse.SearchEventResponseList result = eventSearchService.search(request, null);

        // then - 정확 일치(id=1)가 첫 번째
        assertThat(result.getHomeEventResponseList()).hasSize(2);
        assertThat(result.getHomeEventResponseList().get(0).getId()).isEqualTo(1L);
        assertThat(result.getHomeEventResponseList().get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("POPULARITY 정렬 - _score가 같으면 인기도 순으로 정렬된다")
    void search_popularity_sameScoreThenByPopularity() throws Exception {
        // given
        EventDocument lowPop = createDoc(1L, "해커톤 서울", 10.0);
        EventDocument highPop = createDoc(2L, "해커톤 부산", 50.0);

        // _score가 동일하면 ES가 popularity_score 기준으로 정렬
        Hit<EventDocument> hit1 = buildHit(highPop, 8.0);
        Hit<EventDocument> hit2 = buildHit(lowPop, 8.0);

        SearchResponse<EventDocument> response = buildSearchResponse(List.of(hit1, hit2), 2);

        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(EventDocument.class)))
                .thenReturn(response);
        when(eventService.getBookmarkedEventId(any(), any())).thenReturn(Collections.emptySet());

        EventResponse.HomeEventResponse homeResp1 = EventResponse.HomeEventResponse.builder()
                .id(2L).build();
        EventResponse.HomeEventResponse homeResp2 = EventResponse.HomeEventResponse.builder()
                .id(1L).build();

        when(eventMapper.mapEsDocToHomeItem(eq(highPop), eq(8.0), eq(false))).thenReturn(homeResp1);
        when(eventMapper.mapEsDocToHomeItem(eq(lowPop), eq(8.0), eq(false))).thenReturn(homeResp2);
        when(eventMapper.toSearchEventResponseList(eq(2), any(), eq(false)))
                .thenAnswer(inv -> {
                    List<EventResponse.HomeEventResponse> items = inv.getArgument(1);
                    return EventResponse.SearchEventResponseList.builder()
                            .total(2)
                            .homeEventResponseList(items)
                            .fallback(false)
                            .build();
                });

        EventRequest.EventSearchRequest request = buildRequest("해커톤", EventSortType.POPULARITY);

        // when
        EventResponse.SearchEventResponseList result = eventSearchService.search(request, null);

        // then - 인기도 높은 것(id=2)이 먼저
        assertThat(result.getHomeEventResponseList()).hasSize(2);
        assertThat(result.getHomeEventResponseList().get(0).getId()).isEqualTo(2L);
        assertThat(result.getHomeEventResponseList().get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("LATEST 정렬 - _score와 무관하게 최신순으로 정렬된다")
    void search_latest_sortByCreatedAt() throws Exception {
        // given
        EventDocument older = createDoc(1L, "해커톤 A", 10.0);
        EventDocument newer = createDoc(2L, "해커톤 B", 5.0);

        Hit<EventDocument> hit1 = buildHit(newer, 3.0);
        Hit<EventDocument> hit2 = buildHit(older, 10.0);

        SearchResponse<EventDocument> response = buildSearchResponse(List.of(hit1, hit2), 2);

        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(EventDocument.class)))
                .thenReturn(response);
        when(eventService.getBookmarkedEventId(any(), any())).thenReturn(Collections.emptySet());

        EventResponse.HomeEventResponse homeResp1 = EventResponse.HomeEventResponse.builder()
                .id(2L).build();
        EventResponse.HomeEventResponse homeResp2 = EventResponse.HomeEventResponse.builder()
                .id(1L).build();

        when(eventMapper.mapEsDocToHomeItem(eq(newer), eq(3.0), eq(false))).thenReturn(homeResp1);
        when(eventMapper.mapEsDocToHomeItem(eq(older), eq(10.0), eq(false))).thenReturn(homeResp2);
        when(eventMapper.toSearchEventResponseList(eq(2), any(), eq(false)))
                .thenAnswer(inv -> {
                    List<EventResponse.HomeEventResponse> items = inv.getArgument(1);
                    return EventResponse.SearchEventResponseList.builder()
                            .total(2)
                            .homeEventResponseList(items)
                            .fallback(false)
                            .build();
                });

        EventRequest.EventSearchRequest request = buildRequest("해커톤", EventSortType.LATEST);

        // when
        EventResponse.SearchEventResponseList result = eventSearchService.search(request, null);

        // then - LATEST는 _score 무시하고 created_at 기준
        assertThat(result.getHomeEventResponseList()).hasSize(2);
        assertThat(result.getHomeEventResponseList().get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("DEADLINE 정렬 - recruit_end 오름차순으로 정렬된다")
    void search_deadline_sortByRecruitEnd() throws Exception {
        // given
        EventDocument soonDeadline = createDoc(1L, "해커톤 A", 10.0);
        EventDocument laterDeadline = createDoc(2L, "해커톤 B", 50.0);

        Hit<EventDocument> hit1 = buildHit(soonDeadline, 5.0);
        Hit<EventDocument> hit2 = buildHit(laterDeadline, 5.0);

        SearchResponse<EventDocument> response = buildSearchResponse(List.of(hit1, hit2), 2);

        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(EventDocument.class)))
                .thenReturn(response);
        when(eventService.getBookmarkedEventId(any(), any())).thenReturn(Collections.emptySet());

        EventResponse.HomeEventResponse homeResp1 = EventResponse.HomeEventResponse.builder()
                .id(1L).build();
        EventResponse.HomeEventResponse homeResp2 = EventResponse.HomeEventResponse.builder()
                .id(2L).build();

        when(eventMapper.mapEsDocToHomeItem(eq(soonDeadline), eq(5.0), eq(false))).thenReturn(homeResp1);
        when(eventMapper.mapEsDocToHomeItem(eq(laterDeadline), eq(5.0), eq(false))).thenReturn(homeResp2);
        when(eventMapper.toSearchEventResponseList(eq(2), any(), eq(false)))
                .thenAnswer(inv -> {
                    List<EventResponse.HomeEventResponse> items = inv.getArgument(1);
                    return EventResponse.SearchEventResponseList.builder()
                            .total(2)
                            .homeEventResponseList(items)
                            .fallback(false)
                            .build();
                });

        EventRequest.EventSearchRequest request = buildRequest("해커톤", EventSortType.DEADLINE);

        // when
        EventResponse.SearchEventResponseList result = eventSearchService.search(request, null);

        // then
        assertThat(result.getHomeEventResponseList()).hasSize(2);
        assertThat(result.getHomeEventResponseList().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("검색 결과가 0건이면 fallback 인기 행사 목록을 반환한다")
    void search_noResults_returnsFallback() throws Exception {
        // given
        SearchResponse<EventDocument> emptyResponse = buildSearchResponse(List.of(), 0);

        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(EventDocument.class)))
                .thenReturn(emptyResponse);
        when(eventRepository.findPopularForHomeWithPopularity(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(eventService.getBookmarkedEventId(any(), any())).thenReturn(Collections.emptySet());
        when(eventMapper.toSearchEventResponseList(eq(0), any(), eq(true)))
                .thenReturn(EventResponse.SearchEventResponseList.builder()
                        .total(0)
                        .homeEventResponseList(Collections.emptyList())
                        .fallback(true)
                        .build());

        EventRequest.EventSearchRequest request = buildRequest("존재하지않는행사", EventSortType.POPULARITY);

        // when
        EventResponse.SearchEventResponseList result = eventSearchService.search(request, null);

        // then
        assertThat(result.isFallback()).isTrue();
        assertThat(result.getTotal()).isEqualTo(0);
    }
}
