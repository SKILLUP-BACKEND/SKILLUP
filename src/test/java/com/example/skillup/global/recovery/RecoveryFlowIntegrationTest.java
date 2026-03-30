package com.example.skillup.global.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.events.EventCreatedEvent;
import com.example.skillup.domain.event.events.ThumbnailUploadedEvent;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.recovery.entity.FileCleanupFailure;
import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.enums.RetryStatus;
import com.example.skillup.global.recovery.repository.FileCleanupFailureRepository;
import com.example.skillup.global.recovery.repository.SearchIndexFailureRepository;
import com.example.skillup.global.recovery.service.FileCleanupRetryService;
import com.example.skillup.global.recovery.service.SearchIndexRetryService;
import com.example.skillup.global.search.service.EventIndexerService;
import com.example.skillup.global.service.S3Service;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class RecoveryFlowIntegrationTest {

    @MockitoBean
    private S3Service s3Service;
    @MockitoBean
    private EventRepository eventRepository;
    @MockitoBean
    private EventIndexerService eventIndexerService;

    static class TestTxPublisher {

        private final ApplicationEventPublisher eventPublisher;

        public TestTxPublisher(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Transactional
        public void publishEventCreatedAfterCommit(Long eventId) {
            eventPublisher.publishEvent(new EventCreatedEvent(eventId));
        }

        @Transactional
        public void publishThumbnailUploadedAndRollback(String thumbnailUrl) {
            eventPublisher.publishEvent(new ThumbnailUploadedEvent(ResourceType.EVENT, thumbnailUrl));
            throw new RuntimeException("강제 롤백");
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        TestTxPublisher testTxPublisher(ApplicationEventPublisher eventPublisher) {
            return new TestTxPublisher(eventPublisher);
        }
    }

    @Autowired
    private TestTxPublisher testTxPublisher;

    @Autowired
    private FileCleanupFailureRepository fileCleanupFailureRepository;

    @Autowired
    private SearchIndexFailureRepository searchIndexFailureRepository;

    @Autowired
    private FileCleanupRetryService fileCleanupRetryService;

    @Autowired
    private SearchIndexRetryService searchIndexRetryService;

    @AfterEach
    void tearDown() {
        fileCleanupFailureRepository.deleteAllInBatch();
        searchIndexFailureRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("AFTER_COMMIT 인덱싱 실패 시 search_index_failure 테이블에 저장된다")
    void afterCommit_indexingFail_saveSearchIndexFailure() {
        // given
        Long eventId = 1L;
        Event mockEvent = Mockito.mock(Event.class);

        when(eventRepository.getEvent(eventId)).thenReturn(mockEvent);
        doThrow(new RuntimeException("ES 연결 실패"))
                .when(eventIndexerService).index(mockEvent);

        // when
        testTxPublisher.publishEventCreatedAfterCommit(eventId);

        // then
        List<SearchIndexFailure> failures = searchIndexFailureRepository.findAll();
        assertThat(failures).hasSize(1);

        SearchIndexFailure failure = failures.get(0);
        assertThat(failure.getStatus()).isEqualTo(RetryStatus.PENDING);
        assertThat(failure.getRetryCount()).isEqualTo(1);
        assertThat(failure.getResourceType()).isEqualTo(ResourceType.EVENT);
        assertThat(failure.getResourceId()).isEqualTo(eventId);
        assertThat(failure.getIndexName()).isEqualTo("events_v3");
        assertThat(failure.getDocumentId()).isEqualTo(String.valueOf(eventId));
        assertThat(failure.getFailureReason()).contains("Elastic Search 인덱싱 실패: ES 연결 실패");
    }

    @Test
    @DisplayName("AFTER_ROLLBACK 썸네일 삭제 실패 시 file_cleanup_failure 테이블에 저장된다")
    void afterRollback_thumbnailDeleteFail_saveFileCleanupFailure() {
        // given
        String thumbnailUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/event/thumb/a.png";

        doThrow(new RuntimeException("S3 삭제 실패"))
                .when(s3Service).deleteFileFromUrl(thumbnailUrl);

        // when
        assertThatThrownBy(() -> testTxPublisher.publishThumbnailUploadedAndRollback(thumbnailUrl))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("강제 롤백");

        // then
        List<FileCleanupFailure> failures = fileCleanupFailureRepository.findAll();
        assertThat(failures).hasSize(1);

        FileCleanupFailure failure = failures.get(0);
        assertThat(failure.getStatus()).isEqualTo(RetryStatus.PENDING);
        assertThat(failure.getRetryCount()).isEqualTo(1);
        assertThat(failure.getFileUrl()).isEqualTo(thumbnailUrl);
        assertThat(failure.getResourceType()).isEqualTo(ResourceType.EVENT);
        assertThat(failure.getResourceId()).isNull();
        assertThat(failure.getFailureReason()).contains("S3 이미지 롤백 실패");
        assertThat(failure.getFailureReason()).contains("S3 삭제 실패");
    }

    @Test
    @DisplayName("파일 정리 재처리 성공 시 SUCCESS 상태로 변경된다")
    void fileCleanupRetry_success_markSuccess() {
        // given
        String thumbnailUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/event/thumb/b.png";

        FileCleanupFailure failure = FileCleanupFailure.of(
                ResourceType.EVENT,
                null,
                thumbnailUrl,
                null,
                null,
                "초기 실패"
        );
        fileCleanupFailureRepository.save(failure);

        doNothing().when(s3Service).deleteFileFromUrl(thumbnailUrl);

        // when
        fileCleanupRetryService.retryPendingFailures();

        // then
        FileCleanupFailure saved = fileCleanupFailureRepository.findAll().get(0);
        assertThat(saved.getStatus()).isEqualTo(RetryStatus.SUCCEEDED);
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("검색 인덱스 재처리 반복 실패 시 FAILED 상태로 변경된다")
    void searchIndexRetry_failUntilMax_markFailed() {
        // given
        Long eventId = 10L;
        Event mockEvent = Mockito.mock(Event.class);

        SearchIndexFailure failure = SearchIndexFailure.of(
                ResourceType.EVENT,
                eventId,
                "events_v3",
                String.valueOf(eventId),
                "최초 실패"
        );
        failure.increaseRetryCount("두 번째 실패");
        searchIndexFailureRepository.save(failure);

        when(eventRepository.getEvent(eventId)).thenReturn(mockEvent);
        doThrow(new RuntimeException("재시도도 실패"))
                .when(eventIndexerService).index(any(Event.class));

        // when
        searchIndexRetryService.retryPendingFailures();

        // then
        SearchIndexFailure saved = searchIndexFailureRepository.findAll().get(0);
        assertThat(saved.getRetryCount()).isEqualTo(3);
        assertThat(saved.getStatus()).isEqualTo(RetryStatus.FAILED);
        assertThat(saved.getFailureReason()).contains("검색 인덱스 재처리 실패");
        assertThat(saved.getFailureReason()).contains("재시도도 실패");
        assertThat(saved.getProcessedAt()).isNull();
        assertThat(saved.getLastTriedAt()).isNotNull();
    }
}