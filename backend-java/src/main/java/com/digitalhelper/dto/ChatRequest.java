package com.digitalhelper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChatRequest(
        @JsonProperty("messages") List<Message> messages,
        @JsonProperty("session_id") String sessionId
) {}
