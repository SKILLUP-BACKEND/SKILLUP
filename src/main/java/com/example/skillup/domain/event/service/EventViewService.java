package com.example.skillup.domain.event.service;


import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.EventViewDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventViewService {

    private final EventViewDailyRepository eventViewDailyRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void recordView(Long eventId) {
        eventViewDailyRepository.upsertToday(eventId);   // 일자 집계 +1 (UPSERT)
        eventRepository.incrementViews(eventId); // 총합 +1
    }

    @Transactional(readOnly = true)
    public long getLast14DaysTotal(Long eventId) {
        return eventViewDailyRepository.sumLast14Days(eventId);
    }
}
