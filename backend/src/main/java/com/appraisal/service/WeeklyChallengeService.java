package com.appraisal.service;

import com.appraisal.dto.ChallengeStatusResponse;
import com.appraisal.dto.SpinResponse;
import com.appraisal.dto.SubmissionResponse;
import com.appraisal.entity.WeeklyChallengeAssignment;
import com.appraisal.repository.WeeklyChallengeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class WeeklyChallengeService {

    private static final ZoneId APPLICATION_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final int POINTS_PER_CHALLENGE = 20;

    private static final Map<String, String> CHALLENGES =
            new LinkedHashMap<>();

    static {
        CHALLENGES.put(
            "BUSINESS_CHALLENGE_SOLVER",
            "Business Challenge Solver"
        );

        CHALLENGES.put(
            "COMMUNITY_CONTRIBUTOR",
            "Community Contributor"
        );

        CHALLENGES.put(
            "COMPANY_QUIZ_CREATOR",
            "Company Quiz Creator"
        );

        CHALLENGES.put(
            "WEBINAR_CHAMPION",
            "Webinar Champion"
        );

        CHALLENGES.put(
            "COMPANY_SPOTLIGHT_POST",
            "Company Spotlight Post"
        );
    }

    private final WeeklyChallengeRepository repository;
    private final Path uploadRoot;

    public WeeklyChallengeService(
            WeeklyChallengeRepository repository,
            @Value("${app.upload-dir:uploads/challenges}")
            String uploadDirectory) {

        this.repository = repository;
        this.uploadRoot = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public ChallengeStatusResponse getStatus(Long employeeId) {

        LocalDateTime now = LocalDateTime.now(APPLICATION_ZONE);
        LocalDate today = now.toLocalDate();
        LocalDate currentMonday = getCurrentMonday(today);

        List<WeeklyChallengeAssignment> completedAssignments =
                repository.findByEmployeeIdAndCompletedTrue(employeeId);

        Set<String> completedKeys = new HashSet<>();

        for (WeeklyChallengeAssignment assignment :
                completedAssignments) {

            completedKeys.add(assignment.getChallengeKey());
        }

        List<String> completedNames = completedKeys.stream()
                .map(CHALLENGES::get)
                .filter(Objects::nonNull)
                .toList();

        List<String> availableNames = CHALLENGES.entrySet()
                .stream()
                .filter(entry ->
                        !completedKeys.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        boolean allCompleted =
                completedKeys.size() >= CHALLENGES.size();

        Optional<WeeklyChallengeAssignment> currentAssignment =
                repository.findByEmployeeIdAndWeekStart(
                        employeeId,
                        currentMonday
                );

        boolean monday = today.getDayOfWeek() == DayOfWeek.MONDAY;

        boolean canSpin =
                monday &&
                currentAssignment.isEmpty() &&
                !allCompleted;

        if (currentAssignment.isPresent()) {

            WeeklyChallengeAssignment assignment =
                    currentAssignment.get();

            boolean canSubmit =
                    !assignment.isCompleted() &&
                    !now.isAfter(assignment.getDueAt());

            String message;

            if (assignment.isCompleted()) {
                message =
                    "This week's challenge has been completed. " +
                    "Your next spin will open on Monday.";
            } else if (now.isAfter(assignment.getDueAt())) {
                message =
                    "The submission deadline for this challenge " +
                    "has passed. You can spin again next Monday.";
            } else {
                message =
                    "You have already spun this week. " +
                    "Complete and submit your assigned challenge " +
                    "before Friday.";
            }

            return new ChallengeStatusResponse(
                    false,
                    canSubmit,
                    allCompleted,
                    message,
                    assignment.getChallengeKey(),
                    assignment.getChallengeName(),
                    assignment.getWeekStart(),
                    assignment.getDueAt(),
                    completedKeys.size(),
                    CHALLENGES.size(),
                    completedKeys.size() * POINTS_PER_CHALLENGE,
                    completedNames,
                    availableNames
            );
        }

        String message;

        if (allCompleted) {
            message =
                "Congratulations! You have completed all challenges.";
        } else if (monday) {
            message =
                "Your weekly spin is available.";
        } else {
            LocalDate nextMonday = today.with(
                    TemporalAdjusters.next(DayOfWeek.MONDAY)
            );

            message =
                "The wheel opens only on Monday. Next spin date: " +
                nextMonday;
        }

        return new ChallengeStatusResponse(
                canSpin,
                false,
                allCompleted,
                message,
                null,
                null,
                null,
                null,
                completedKeys.size(),
                CHALLENGES.size(),
                completedKeys.size() * POINTS_PER_CHALLENGE,
                completedNames,
                availableNames
        );
    }

    @Transactional
    public SpinResponse spin(Long employeeId) {

        LocalDateTime now = LocalDateTime.now(APPLICATION_ZONE);
        LocalDate today = now.toLocalDate();

        if (today.getDayOfWeek() != DayOfWeek.MONDAY) {
            return new SpinResponse(
                    false,
                    "The wheel can be spun only on Monday.",
                    null,
                    null,
                    null
            );
        }

        LocalDate monday = getCurrentMonday(today);

        Optional<WeeklyChallengeAssignment> existing =
                repository.findByEmployeeIdAndWeekStart(
                        employeeId,
                        monday
                );

        if (existing.isPresent()) {
            WeeklyChallengeAssignment assignment = existing.get();

            return new SpinResponse(
                    false,
                    "You have already spun the wheel this week.",
                    assignment.getChallengeKey(),
                    assignment.getChallengeName(),
                    assignment.getDueAt()
            );
        }

        Set<String> completedKeys =
                new HashSet<>();

        for (WeeklyChallengeAssignment assignment :
                repository.findByEmployeeIdAndCompletedTrue(employeeId)) {

            completedKeys.add(assignment.getChallengeKey());
        }

        List<Map.Entry<String, String>> available =
                CHALLENGES.entrySet()
                        .stream()
                        .filter(entry ->
                                !completedKeys.contains(entry.getKey()))
                        .toList();

        if (available.isEmpty()) {
            return new SpinResponse(
                    false,
                    "You have completed all available challenges.",
                    null,
                    null,
                    null
            );
        }

        Map.Entry<String, String> selected =
                available.get(
                    new Random().nextInt(available.size())
                );

        LocalDate friday = monday.plusDays(4);

        WeeklyChallengeAssignment assignment =
                new WeeklyChallengeAssignment();

        assignment.setEmployeeId(employeeId);
        assignment.setWeekStart(monday);
        assignment.setChallengeKey(selected.getKey());
        assignment.setChallengeName(selected.getValue());
        assignment.setAssignedAt(now);

        // Submission remains open through Friday.
        assignment.setDueAt(
                LocalDateTime.of(friday, LocalTime.MAX)
        );

        assignment.setCompleted(false);

        repository.save(assignment);

        return new SpinResponse(
                true,
                "Challenge assigned successfully. " +
                "Submit it before Friday.",
                assignment.getChallengeKey(),
                assignment.getChallengeName(),
                assignment.getDueAt()
        );
    }

    @Transactional
    public SubmissionResponse submit(
            Long employeeId,
            MultipartFile file) throws IOException {

        LocalDateTime now = LocalDateTime.now(APPLICATION_ZONE);
        LocalDate monday = getCurrentMonday(now.toLocalDate());

        WeeklyChallengeAssignment assignment =
                repository.findByEmployeeIdAndWeekStart(
                        employeeId,
                        monday
                ).orElseThrow(() ->
                        new IllegalStateException(
                            "No challenge has been assigned this week."
                        )
                );

        if (assignment.isCompleted()) {
            throw new IllegalStateException(
                    "This challenge has already been submitted."
            );
        }

        if (now.isAfter(assignment.getDueAt())) {
            throw new IllegalStateException(
                    "Submission is closed. The Friday deadline has passed."
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a file to upload."
            );
        }

        validateFile(file);

        Path employeeFolder = uploadRoot.resolve(
                String.valueOf(employeeId)
        );

        Files.createDirectories(employeeFolder);

        String originalFileName =
                Optional.ofNullable(file.getOriginalFilename())
                        .orElse("submission");

        originalFileName = Paths.get(originalFileName)
                .getFileName()
                .toString();

        String storedFileName =
                UUID.randomUUID() + "_" + originalFileName;

        Path targetFile = employeeFolder
                .resolve(storedFileName)
                .normalize();

        if (!targetFile.startsWith(employeeFolder)) {
            throw new SecurityException("Invalid file path.");
        }

        Files.copy(
                file.getInputStream(),
                targetFile,
                StandardCopyOption.REPLACE_EXISTING
        );

        assignment.setOriginalFileName(originalFileName);
        assignment.setStoredFileName(storedFileName);
        assignment.setSubmittedAt(now);
        assignment.setCompleted(true);

        repository.save(assignment);

        int completedCount =
                repository
                    .findByEmployeeIdAndCompletedTrue(employeeId)
                    .size();

        return new SubmissionResponse(
                true,
                "Challenge submitted successfully. " +
                "It will not appear in your wheel again.",
                completedCount,
                completedCount * POINTS_PER_CHALLENGE
        );
    }

    private void validateFile(MultipartFile file) {

        long maximumSize = 10L * 1024L * 1024L;

        if (file.getSize() > maximumSize) {
            throw new IllegalArgumentException(
                    "The maximum allowed file size is 10 MB."
            );
        }

        String contentType = file.getContentType();

        Set<String> allowedTypes = Set.of(
                "image/png",
                "image/jpeg",
                "application/pdf"
        );

        if (contentType == null ||
                !allowedTypes.contains(contentType)) {

            throw new IllegalArgumentException(
                    "Only PNG, JPG, JPEG, and PDF files are allowed."
            );
        }
    }

    private LocalDate getCurrentMonday(LocalDate date) {
        return date.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY
                )
        );
    }
}