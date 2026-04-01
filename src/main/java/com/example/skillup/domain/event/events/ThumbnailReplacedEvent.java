package com.example.skillup.domain.event.events;

import com.example.skillup.global.recovery.enums.ResourceType;

public record ThumbnailReplacedEvent(ResourceType resourceType,Long resourceId, String oldThumbnailUrl) {
}
