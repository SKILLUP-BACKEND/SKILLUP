package com.example.skillup.domain.event.events;

import com.example.skillup.global.recovery.enums.ResourceType;

public record ThumbnailUploadedEvent(ResourceType resourceType, String thumbnailUrl) {
}
