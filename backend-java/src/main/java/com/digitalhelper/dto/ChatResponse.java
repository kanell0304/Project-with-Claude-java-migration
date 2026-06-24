package com.digitalhelper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatResponse(
        @JsonProperty("reply") String reply,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("guide_used") boolean guideUsed,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("needs_app_selection") boolean needsAppSelection
) {}
