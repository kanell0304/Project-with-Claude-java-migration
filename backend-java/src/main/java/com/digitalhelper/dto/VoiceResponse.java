package com.digitalhelper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VoiceResponse(
        @JsonProperty("transcript") String transcript
) {}
