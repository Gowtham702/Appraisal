package com.appraisal.dto;

import java.time.LocalDateTime;

public record SpinResponse(
    boolean success,
    String message,
    String challengeKey,
    String challengeName,
    LocalDateTime dueAt
) {
}
