package com.appraisal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ChallengeStatusResponse(
    boolean canSpin,
    boolean canSubmit,
    boolean allChallengesCompleted,
    String message,
    String currentChallengeKey,
    String currentChallengeName,
    LocalDate weekStart,
    LocalDateTime dueAt,
    int completedCount,
    int totalChallenges,
    int totalPoints,
    List<String> completedChallenges,
    List<String> availableChallenges
) {
}