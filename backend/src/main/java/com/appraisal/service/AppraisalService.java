package com.appraisal.service;

import com.appraisal.model.Employee;
import com.appraisal.model.FormResponse;
import com.appraisal.model.FormResponse.ResponseType;
import com.appraisal.repository.EmployeeRepository;
import com.appraisal.repository.FormRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class AppraisalService {

    private static final Set<String> ALLOWED_CHALLENGES =
        Set.of(
            "Business Challenge Solver",
            "Community Contributor",
            "Company Quiz Creator",
            "Webinar Champion",
            "Company Spotlight Post"
        );

    private final EmployeeRepository employeeRepository;
    private final FormRepository formRepository;
    private final Path uploadRoot;

    public AppraisalService(
        EmployeeRepository employeeRepository,
        FormRepository formRepository,
        @Value("${app.upload-dir:uploads}") String uploadDirectory
    ) {
        this.employeeRepository = employeeRepository;
        this.formRepository = formRepository;

        this.uploadRoot = Paths
            .get(uploadDirectory)
            .toAbsolutePath()
            .normalize();

        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Could not create upload directory",
                exception
            );
        }
    }

    @Transactional
    public Employee saveEmployee(Employee request) {

        String employeeCode =
            request.getEmployeeCode().trim();

        Employee employee = employeeRepository
            .findByEmployeeCode(employeeCode)
            .orElseGet(Employee::new);

        employee.setEmployeeCode(employeeCode);
        employee.setEmployeeName(
            request.getEmployeeName().trim()
        );

        employee.setDepartment(
            cleanValue(request.getDepartment())
        );

        employee.setDesignation(
            cleanValue(request.getDesignation())
        );

        employee.setCurrentRole(
            cleanValue(request.getCurrentRole())
        );

        employee.setDateOfJoining(
            request.getDateOfJoining()
        );

        return employeeRepository.save(employee);
    }

    public Employee getEmployee(Long employeeId) {

        return employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> new NoSuchElementException(
                    "Employee not found: " + employeeId
                )
            );
    }

    @Transactional
    public FormResponse saveAnswer(
        Long employeeId,
        Map<String, String> request
    ) {

        Employee employee = getEmployee(employeeId);

        String phase =
            required(request.get("phase"), "phase");

        String questionKey =
            required(
                request.get("questionKey"),
                "questionKey"
            );

        String answer =
            required(request.get("answer"), "answer");

        FormResponse response = new FormResponse();

        response.setEmployee(employee);

        response.setResponseType(
            ResponseType.APPRAISAL_ANSWER
        );

        response.setPhase(phase);
        response.setQuestionKey(questionKey);
        response.setAnswer(answer);
        response.setPointsAwarded(0);

        return formRepository.save(response);
    }

    public List<FormResponse> getResponses(
        Long employeeId
    ) {

        getEmployee(employeeId);

        return formRepository
            .findByEmployeeIdOrderBySubmittedAtAsc(
                employeeId
            );
    }

    @Transactional
    public FormResponse submitChallenge(
        Long employeeId,
        String challengeName,
        MultipartFile file
    ) throws IOException {

        Employee employee =
            getEmployee(employeeId);

        String challenge =
            required(challengeName, "challengeName");

        if (!ALLOWED_CHALLENGES.contains(challenge)) {
            throw new IllegalArgumentException(
                "Invalid challenge name"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                "Please upload a file"
            );
        }

        long maximumSize =
            20L * 1024 * 1024;

        if (file.getSize() > maximumSize) {
            throw new IllegalArgumentException(
                "File must be 20 MB or smaller"
            );
        }

        boolean alreadyCompleted =
            formRepository
                .existsByEmployeeIdAndResponseTypeAndChallengeName(
                    employeeId,
                    ResponseType.CHALLENGE_SUBMISSION,
                    challenge
                );

        if (alreadyCompleted) {
            throw new IllegalStateException(
                "This challenge has already been submitted"
            );
        }

        String originalName =
            Optional
                .ofNullable(file.getOriginalFilename())
                .orElse("submission");

        String safeName =
            Paths
                .get(originalName)
                .getFileName()
                .toString()
                .replaceAll(
                    "[^A-Za-z0-9._-]",
                    "_"
                );

        String storedName =
            employeeId
            + "_"
            + UUID.randomUUID()
            + "_"
            + safeName;

        Path employeeFolder =
            uploadRoot.resolve(
                String.valueOf(employeeId)
            );

        Files.createDirectories(employeeFolder);

        Path destination =
            employeeFolder
                .resolve(storedName)
                .normalize();

        if (!destination.startsWith(employeeFolder)) {
            throw new IllegalArgumentException(
                "Invalid file name"
            );
        }

        Files.copy(
            file.getInputStream(),
            destination,
            StandardCopyOption.REPLACE_EXISTING
        );

        FormResponse response =
            new FormResponse();

        response.setEmployee(employee);

        response.setResponseType(
            ResponseType.CHALLENGE_SUBMISSION
        );

        response.setChallengeName(challenge);
        response.setOriginalFileName(originalName);
        response.setStoredFileName(storedName);
        response.setFilePath(destination.toString());
        response.setContentType(file.getContentType());
        response.setFileSize(file.getSize());
        response.setPointsAwarded(20);

        return formRepository.save(response);
    }

    public Map<String, Object> getProgress(
        Long employeeId
    ) {

        getEmployee(employeeId);

        long completedCount =
            formRepository
                .countByEmployeeIdAndResponseType(
                    employeeId,
                    ResponseType.CHALLENGE_SUBMISSION
                );

        int completed =
            (int) Math.min(completedCount, 5);

        int points =
            completed * 20;

        int week =
            Math.min(completed + 1, 5);

        Map<String, Object> result =
            new LinkedHashMap<>();

        result.put("employeeId", employeeId);
        result.put("completedChallenges", completed);
        result.put("totalChallenges", 5);
        result.put("points", points);
        result.put("progressPercent", points);
        result.put("week", week);
        result.put("completed", completed == 5);

        return result;
    }

    private static String required(
        String value,
        String fieldName
    ) {

        if (
            value == null ||
            value.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                fieldName + " is required"
            );
        }

        return value.trim();
    }

    private static String cleanValue(
        String value
    ) {

        if (
            value == null ||
            value.trim().isEmpty()
        ) {
            return null;
        }

        return value.trim();
    }
}