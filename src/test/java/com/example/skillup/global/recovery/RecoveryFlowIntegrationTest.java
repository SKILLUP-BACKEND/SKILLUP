package com.example.skillup.global.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.events.EventCreatedEvent;
import com.example.skillup.domain.event.events.ThumbnailUploadedEvent;
import com.example.skillup.domain.event.listener.EventIndexingListener;
import com.example.skillup.domain.event.listener.S3RollbackCleanupListener;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.recovery.entity.FileCleanupFailure;
import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.enums.RetryStatus;
import com.example.skillup.global.recovery.repository.FileCleanupFailureRepository;
import com.example.skillup.global.recovery.repository.SearchIndexFailureRepository;
import com.example.skillup.global.recovery.service.FileCleanupFailureSaveService;
import com.example.skillup.global.recovery.service.FileCleanupRetryService;
import com.example.skillup.global.recovery.service.SearchIndexRetryService;
import com.example.skillup.global.search.service.EventIndexerService;
import com.example.skillup.global.service.S3Service;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringJUnitConfig(RecoveryFlowIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
                "spring.datasource.url=jdbc:h2:mem:outbox;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "skillup.search.index-name=events_v3"
        })
class RecoveryFlowIntegrationTest {

    @MockitoBean
    private S3Service s3Service;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockitoSpyBean
    private EventIndexingListener indexingListener;
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

    @TestConfiguration(proxyBeanMethods = false)
    @ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @EntityScan("com.example.skillup")
    @EnableJpaRepositories(basePackages = {
            "com.example.skillup.domain.event.repository", "com.example.skillup.global.recovery.repository"})
    @EnableTransactionManagement
    @Import({EventIndexingListener.class, SearchIndexRetryService.class, S3RollbackCleanupListener.class,
            FileCleanupFailureSaveService.class, FileCleanupRetryService.class})
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
        eventRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("AFTER_COMMIT 인덱싱 실패 시 미리 기록한 작업에 실패 횟수가 남는다")
    void afterCommit_indexingFail_saveSearchIndexFailure() {
        // given
        Long eventId = eventRepository.save(Event.builder().title("행사").status(EventStatus.PUBLISHED).build()).getId();
        doThrow(new RuntimeException("ES 연결 실패"))
                .when(eventIndexerService).index(any(Event.class));

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
        assertThat(failure.getFailureReason()).contains("검색 인덱스 재처리 실패: ES 연결 실패");
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
        Long eventId = eventRepository.save(Event.builder().title("행사").status(EventStatus.PUBLISHED).build()).getId();

        SearchIndexFailure failure = SearchIndexFailure.of(
                ResourceType.EVENT,
                eventId,
                "events_v3",
                String.valueOf(eventId),
                "최초 실패"
        );
        failure.increaseRetryCount("두 번째 실패");
        searchIndexFailureRepository.save(failure);

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

    private Long createAndPublish() {
        return new TransactionTemplate(transactionManager).execute(status -> {
            Event event = eventRepository.save(Event.builder().title("행사").status(EventStatus.PUBLISHED).build());
            testTxPublisher.publishEventCreatedAfterCommit(event.getId());
            return event.getId();
        });
    }

    @Test
    @DisplayName("업무 커밋 시 작업을 기록하고 즉시 처리한다")
    void commitRecordsAndProcessesTask() {
        Long eventId = createAndPublish();
        SearchIndexFailure task = searchIndexFailureRepository.findAll().get(0);
        assertThat(task.getResourceId()).isEqualTo(eventId);
        assertThat(task.getStatus()).isEqualTo(RetryStatus.SUCCEEDED);
        assertThat(task.getRetryCount()).isZero();
        Mockito.verify(eventIndexerService).index(any(Event.class));
    }

    @Test
    @DisplayName("Outbox 기록 후 커밋이 실패하면 행사와 작업이 함께 롤백된다")
    void commitFailureRollsBackEventAndTask() {
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            createAndPublish();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    assertThat(searchIndexFailureRepository.count()).isEqualTo(1);
                    throw new IllegalStateException("커밋 실패");
                }
            });
        })).hasMessage("커밋 실패");
        assertThat(eventRepository.count()).isZero();
        assertThat(searchIndexFailureRepository.count()).isZero();
        Mockito.verifyNoInteractions(eventIndexerService);
    }

    @Test
    @DisplayName("커밋 후 리스너 실행을 놓쳐도 미시도 작업을 스케줄러가 회수한다")
    void schedulerRecoversMissedAfterCommit() {
        doNothing().when(indexingListener).indexEventAfterCommit(any(EventCreatedEvent.class));
        createAndPublish();
        SearchIndexFailure pending = searchIndexFailureRepository.findAll().get(0);
        assertThat(pending.getStatus()).isEqualTo(RetryStatus.PENDING);
        assertThat(pending.getRetryCount()).isZero();
        assertThat(pending.getLastTriedAt()).isNull();
        Mockito.verifyNoInteractions(eventIndexerService);

        searchIndexRetryService.retryPendingFailures();

        assertThat(searchIndexFailureRepository.findById(pending.getId()).orElseThrow().getStatus())
                .isEqualTo(RetryStatus.SUCCEEDED);
        Mockito.verify(eventIndexerService).index(any(Event.class));
    }

    @Test
    @DisplayName("ES 실패 후 같은 작업을 재시도하고 세 번 실패하면 중단한다")
    void failuresReuseTaskAndStopAtThreeAttempts() {
        doThrow(new RuntimeException("ES 중단")).when(eventIndexerService).index(any(Event.class));
        createAndPublish();
        Long taskId = searchIndexFailureRepository.findAll().get(0).getId();
        searchIndexRetryService.retryPendingFailures();
        searchIndexRetryService.retryPendingFailures();
        searchIndexRetryService.retryPendingFailures();
        assertThat(searchIndexFailureRepository.count()).isEqualTo(1);
        SearchIndexFailure task = searchIndexFailureRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(3);
        assertThat(task.getStatus()).isEqualTo(RetryStatus.FAILED);
        Mockito.verify(eventIndexerService, Mockito.times(3)).index(any(Event.class));
    }

    @Test
    @DisplayName("외부 성공 후 결과 커밋을 놓친 작업은 다시 실행할 수 있다")
    void externalSuccessBeforeResultCommitCanBeRetried() {
        Mockito.doAnswer(call -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    throw new IllegalStateException("ES 성공 후 DB 결과 커밋 실패");
                }
            });
            return null;
        }).when(eventIndexerService).index(any(Event.class));
        Long eventId = createAndPublish();
        SearchIndexFailure pending = searchIndexFailureRepository.findAll().get(0);
        assertThat(pending.getStatus()).isEqualTo(RetryStatus.PENDING);

        doNothing().when(eventIndexerService).index(any(Event.class));
        searchIndexRetryService.retryPendingFailures();

        assertThat(searchIndexFailureRepository.findById(pending.getId()).orElseThrow().getStatus())
                .isEqualTo(RetryStatus.SUCCEEDED);
        Mockito.verify(eventIndexerService, Mockito.times(2))
                .index(Mockito.argThat(event -> event.getId().equals(eventId)));
    }

    @Test
    @DisplayName("처리 시점의 최신 상태를 읽고 숨김 및 삭제된 행사는 ES에서 제거한다")
    void pendingTasksRespectLatestVisibilityAndDeletion() {
        doNothing().when(indexingListener).indexEventAfterCommit(any(EventCreatedEvent.class));
        Long hiddenId = createAndPublish();
        Long deletedId = createAndPublish();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventRepository.getEvent(hiddenId).setStatus(EventStatus.HIDDEN);
            eventRepository.getEvent(deletedId).delete();
        });
        searchIndexRetryService.retryPendingFailures();
        Mockito.verify(eventIndexerService).delete(hiddenId);
        Mockito.verify(eventIndexerService).delete(deletedId);
        Mockito.verify(eventIndexerService, Mockito.never()).index(any(Event.class));
        assertThat(searchIndexFailureRepository.findAll()).allMatch(task -> task.getStatus() == RetryStatus.SUCCEEDED);
    }

    @Test
    @DisplayName("리스너와 스케줄러가 같은 작업을 동시에 잡아도 한 번만 실행한다")
    void concurrentProcessorsClaimTaskOnce() throws Exception {
        doNothing().when(indexingListener).indexEventAfterCommit(any(EventCreatedEvent.class));
        Long eventId = createAndPublish();
        CountDownLatch indexing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch retryStarted = new CountDownLatch(1);
        Mockito.doAnswer(call -> {
            indexing.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("검증 중 색인 대기 시간 초과");
            }
            return null;
        }).when(eventIndexerService).index(any(Event.class));
        var executor = Executors.newFixedThreadPool(2);
        try {
            var immediate = executor.submit(() -> searchIndexRetryService.processPendingEvent(eventId));
            assertThat(indexing.await(5, TimeUnit.SECONDS)).isTrue();
            var scheduled = executor.submit(() -> {
                retryStarted.countDown();
                searchIndexRetryService.retryPendingFailures();
            });
            assertThat(retryStarted.await(5, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            immediate.get(5, TimeUnit.SECONDS);
            scheduled.get(5, TimeUnit.SECONDS);
            Mockito.verify(eventIndexerService).index(any(Event.class));
            assertThat(searchIndexFailureRepository.findAll().get(0).getStatus()).isEqualTo(RetryStatus.SUCCEEDED);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }
}
