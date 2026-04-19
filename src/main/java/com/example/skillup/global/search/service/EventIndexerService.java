package com.example.skillup.global.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.search.document.EventDocument;
import com.example.skillup.global.search.exception.SearchErrorCode;
import com.example.skillup.global.search.exception.SearchException;
import com.example.skillup.global.search.mapper.EventDocumentMapper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventIndexerService {

    private final EventRepository eventRepository;
    private final EventDocumentMapper eventDocumentMapper;
    private final ElasticsearchClient elasticsearchClient;

    @Value("${skillup.search.index-name}")
    private String index;

    // 단건 색인/업데이트
    public void index(Event event) {
        try {
            EventDocument eventDocument = eventDocumentMapper.fromEntity(event);
            elasticsearchClient.index(IndexRequest.of(r -> r
                    .index(index)
                    .id(String.valueOf(eventDocument.getId()))
                    .document(eventDocument)
                    .refresh(Refresh.True)
            ));
        } catch (IOException e) {
            throw new SearchException(SearchErrorCode.SEARCH_INDEX_CREATE_ERROR,
                    "행사(id=" + event.getId() + ") 인덱싱 실패: " + e.getMessage());
        }
    }

    // 단건 삭제
    public void delete(Long eventId) {
        try {
            elasticsearchClient.delete(d -> d.index(index).id(String.valueOf(eventId)).refresh(Refresh.True));
        } catch (IOException e) {
            throw new SearchException(SearchErrorCode.SEARCH_INDEX_DELETE_ERROR,
                    "행사(id=" + eventId + ") 인덱스 삭제 실패: " + e.getMessage());
        }
    }

    // 초기 벌크 색인 ( DB→elasticsearchClient)
    @Transactional(readOnly = true)
    public long bulkIndexAll() {
        long total = 0;
        try {

            elasticsearchClient.deleteByQuery(d -> d.index(index)
                    .conflicts(Conflicts.Proceed)
                    .query(q -> q.matchAll(m->m))
            );

            log.info("기존 존재하던 문서 삭제 완료");

            int pageSize = 500;
            int page = 0;

            while (true) {

                Page<Event> slice = eventRepository.findByStatusWithHashTags(EventStatus.PUBLISHED, PageRequest.of(page, pageSize));
                if (slice.isEmpty()) {
                    break;
                }

                List<BulkOperation> ops = slice.stream()
                        .map(eventDocumentMapper::fromEntity)
                        .map(doc -> BulkOperation.of(op -> op.index(idx -> idx
                                .index(index)
                                .id(String.valueOf(doc.getId()))
                                .document(doc)
                        )))
                        .toList();

                var bulkResp = elasticsearchClient.bulk(
                        BulkRequest.of(b -> b.index(index).operations(ops).refresh(Refresh.True)));
                if (Boolean.TRUE.equals(bulkResp.errors())) {
                    throw new SearchException(SearchErrorCode.SEARCH_INDEX_BULK_ERROR,
                            "벌크 인덱싱 중 일부 문서 실패 (page=" + page + ")");
                }
                total += ops.size();
                page++;
            }
            return total;
        } catch (IOException e) {
            throw new SearchException(SearchErrorCode.SEARCH_INDEX_BULK_ERROR,
                    "벌크 인덱싱 중 ES 통신 오류: " + e.getMessage());
        } catch (ElasticsearchException e) {
            throw new SearchException(SearchErrorCode.SEARCH_INDEX_BULK_ERROR,
                    "벌크 인덱싱 중 ES 오류: " + e.getMessage());
        }
    }
}

