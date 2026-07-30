package com.appraisal.dto;

public record SubmissionResponse(
    boolean success,
    String message,
    int completedChallenges,
    int totalPoints
) {
}
