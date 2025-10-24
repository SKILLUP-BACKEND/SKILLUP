package com.example.skillup.domain.event.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDocument {
    private Long id;
    private String title;
    @JsonProperty("thumbnail_url") private String thumbnailUrl;
    private String description;
    private String category;
    @JsonProperty("is_free") private Boolean isFree;
    private Integer price;
    @JsonProperty("is_online") private Boolean isOnline;
    @JsonProperty("location_text") private String locationText;
    @JsonProperty("recruit_start") private Instant recruitStart;
    @JsonProperty("recruit_end")   private Instant recruitEnd;
    @JsonProperty("event_start")   private Instant eventStart;
    @JsonProperty("event_end")     private Instant eventEnd;
    @JsonProperty("popularity_score") private Double popularityScore;
    @JsonProperty("recommended_manual") private Boolean recommendedManual;
    private Boolean ad;
}
