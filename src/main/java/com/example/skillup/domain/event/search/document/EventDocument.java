package com.example.skillup.domain.event.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDocument {

    private Long id;

    private String title; // 제목

    @JsonProperty("thumbnail_url") private String thumbnailUrl;

    private String category;

    @JsonProperty("is_free") private Boolean isFree;//가격 여부
    private Integer price;

    @JsonProperty("is_online") private Boolean isOnline;//온라인 여부
    @JsonProperty("location_text") private String locationText; //온라인이 아니라면 해당 값 반환해줘야함

    @JsonProperty("recruit_start") private Instant recruitStart;
    @JsonProperty("recruit_end")   private Instant recruitEnd;

    @JsonProperty("event_start")   private Instant eventStart; //d-day 설정
    @JsonProperty("event_end")     private Instant eventEnd;

    @JsonProperty("popularity_score") private Double popularityScore;
    @JsonProperty("recommended_manual") private Boolean recommendedManual;

    private Boolean ad;
}
