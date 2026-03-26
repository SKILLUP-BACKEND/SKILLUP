package com.example.skillup.domain.event.validation;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublishValidator {

    private final EventRepository eventRepository;

    public void validateForPublish(Event event) {

        if (event.getRecruitStart() != null && event.getRecruitEnd() != null) {

            if (event.getRecruitStart().isAfter(event.getRecruitEnd())) {
                throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED,
                        "모집 시작일은 모집 마감일보다 이후일 수 없습니다.");
            }
            if (event.getEventStart().isAfter(event.getEventEnd())) {
                throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED,
                        "행사 시작일은 행사 마감일보다 이후일 수 없습니다.");
            }
            if (event.getRecruitEnd().isAfter(event.getEventEnd())) {
                throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED, "모집 마감일은 행사 마감일보다 늦을 수 없습니다.");
            }
        }

        if (event.getIsFree() == null) {
            throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED, "참가비 여부(isFree)가 누락되었습니다.");
        }
        if (Boolean.FALSE.equals(event.getIsFree())) {
            if (event.getPrice() == null || event.getPrice() <= 0) {
                throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED, "유료 행사 price는 1 이상으로 필수입니다.");
            }
        }

        if (event.getIsOnline() == null) {
            throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED, "온라인/오프라인 여부(isOnline)가 누락되었습니다.");
        }
        if (Boolean.FALSE.equals(event.getIsOnline())) {
            if (event.getLocationText() == null || event.getLocationText().isBlank()) {
                throw new EventException(EventErrorCode.EVENT_PUBLISH_VALIDATION_FAILED,
                        "오프라인 행사는 장소(locationText)가 필수입니다.");
            }
        }
    }

    public void validateDuplicateTitleForCreate(String title) {
        if (eventRepository.existsByTitle(title)) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_PUBLISHED , title + "인 행사가 이미 존재합니다.");
        }
    }

    public void validateDuplicateTitleForUpdate(Long eventId, String title) {
        if (eventRepository.existsByTitleAndIdNot(title, eventId)) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_PUBLISHED , title + "인 행사가  이미 존재합니다.");
        }
    }
}
