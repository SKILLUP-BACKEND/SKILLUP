package com.example.skillup.domain.event.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.mapper.EventDocumentMapper;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.search.document.EventDocument;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventIndexerService {

    private final EventRepository eventRepository;
    private final EventDocumentMapper eventDocumentMapper;
    private final ElasticsearchClient elasticsearchClient;

    private static final String INDEX = "events_v1";

    // 단건 색인/업데이트
    public void index(Event event) {
        try {
            EventDocument eventDocument = eventDocumentMapper.fromEntity(event);
            elasticsearchClient.index(IndexRequest.of(r -> r
                    .index(INDEX)
                    .id(String.valueOf(eventDocument.getId()))
                    .document(eventDocument)
                    .refresh(Refresh.True)
            ));
        } catch (IOException e) {
            throw new EventException(EventErrorCode.EVENT_INDEXING_ERROR, "elasticsearch 생성/업데이트 시 에러 발생");
        }
    }

    // 단건 삭제
    public void delete(Long eventId) {
        try {
            elasticsearchClient.delete(d -> d.index(INDEX).id(String.valueOf(eventId)).refresh(Refresh.True));
        } catch (IOException e) {
            throw new EventException(EventErrorCode.EVENT_INDEXING_ERROR, "elasticsearch 인덱싱 삭제시 에러 발생");
        }
    }

    // 초기 벌크 색인 ( DB→elasticsearchClient)
    @Transactional(readOnly = true)
    public long bulkIndexAll() {
        long total = 0;
        try {
            int pageSize = 500;
            int page = 0;

            while (true) {

                Page<Event> slice = eventRepository.findAll(PageRequest.of(page, pageSize));
                if (slice.isEmpty()) {
                    break;
                }

                List<BulkOperation> ops = slice.stream()
                        .map(eventDocumentMapper::fromEntity)
                        .map(doc -> BulkOperation.of(op -> op.index(idx -> idx
                                .index(INDEX)
                                .id(String.valueOf(doc.getId()))
                                .document(doc)
                        )))
                        .toList();

                var bulkResp = elasticsearchClient.bulk(
                        BulkRequest.of(b -> b.index(INDEX).operations(ops).refresh(Refresh.True)));
                if (Boolean.TRUE.equals(bulkResp.errors())) {
                    throw new EventException(EventErrorCode.EVENT_INDEXING_ERROR, "일부 문서 인덱싱 실패");
                }
                total += ops.size();
                page++;
            }
            return total;
        } catch (IOException e) {
            throw new EventException(EventErrorCode.EVENT_INDEXING_ERROR, "ES 통신 오류");
        } catch (ElasticsearchException e) {
            throw new EventException(EventErrorCode.EVENT_INDEXING_ERROR, e.getMessage());
        }
    }
}

