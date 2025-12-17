package com.example.skillup.global.component;

import com.example.skillup.domain.event.repository.EventActionRepository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Calculator {

    private Calculator() {
    }
    public static Map<YearMonth, Integer> countByMonth(
            List<EventActionRepository.EventActionAnalyticsProjection> actions
    ) {
        return actions.stream()
                .collect(Collectors.groupingBy(
                        a -> YearMonth.from(a.getCreatedAt()),
                        Collectors.summingInt(e -> 1)
                ));
    }

    public static Map<String, Integer> calculateWithHamilton(
            Map<String, Integer> countMap,
            int totalCount
    ) {
        if (totalCount == 0 || countMap.isEmpty()) {
            return Map.of();
        }

        class Temp {
            String key;
            int floor;
            double remainder;

            Temp(String key, int floor, double remainder) {
                this.key = key;
                this.floor = floor;
                this.remainder = remainder;
            }
        }

        List<Temp> temps = new ArrayList<>();
        int sum = 0;

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            double exact = entry.getValue() * 100.0 / totalCount;
            int floor = (int) exact;
            double remainder = exact - floor;

            temps.add(new Temp(entry.getKey(), floor, remainder));
            sum += floor;
        }

        int remain = 100 - sum;

        temps.sort((a, b) -> Double.compare(b.remainder, a.remainder));

        for (int i = 0; i < remain; i++) {
            temps.get(i).floor++;
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Temp temp : temps) {
            result.put(temp.key, temp.floor);
        }

        return result;
    }
}