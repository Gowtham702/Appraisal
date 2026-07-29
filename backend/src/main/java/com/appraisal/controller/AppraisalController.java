package com.appraisal.controller;

import com.appraisal.model.Employee;
import com.appraisal.model.FormResponse;
import com.appraisal.service.AppraisalService;

import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(
    origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://127.0.0.1:8080",
        "http://localhost:8080"
    }
)
public class AppraisalController {

    private final AppraisalService appraisalService;

    public AppraisalController(
        AppraisalService appraisalService
    ) {
        this.appraisalService =
            appraisalService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {

        return Map.of(
            "status", "UP",
            "message",
            "Employee appraisal backend is running"
        );
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> saveEmployee(
        @Valid @RequestBody Employee employee
    ) {

        Employee savedEmployee =
            appraisalService.saveEmployee(employee);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedEmployee);
    }

    @GetMapping("/employees/{employeeId}")
    public Employee getEmployee(
        @PathVariable Long employeeId
    ) {

        return appraisalService
            .getEmployee(employeeId);
    }

    @PostMapping(
        "/forms/{employeeId}/answers"
    )
    public ResponseEntity<FormResponse> saveAnswer(
        @PathVariable Long employeeId,
        @RequestBody Map<String, String> request
    ) {

        FormResponse response =
            appraisalService.saveAnswer(
                employeeId,
                request
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/forms/{employeeId}")
    public List<Map<String, Object>> getResponses(
        @PathVariable Long employeeId
    ) {

        return appraisalService
            .getResponses(employeeId)
            .stream()
            .map(this::convertResponse)
            .toList();
    }

    @PostMapping(
        value =
            "/challenges/{employeeId}/submit",
        consumes =
            MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>>
    submitChallenge(
        @PathVariable Long employeeId,
        @RequestParam String challengeName,
        @RequestPart MultipartFile file
    ) throws IOException {

        FormResponse saved =
            appraisalService.submitChallenge(
                employeeId,
                challengeName,
                file
            );

        Map<String, Object> response =
            new LinkedHashMap<>();

        response.put(
            "message",
            "Challenge submitted successfully"
        );

        response.put(
            "submissionId",
            saved.getId()
        );

        response.put(
            "challengeName",
            saved.getChallengeName()
        );

        response.put(
            "fileName",
            saved.getOriginalFileName()
        );

        response.put(
            "pointsAwarded",
            saved.getPointsAwarded()
        );

        response.put(
            "progress",
            appraisalService.getProgress(
                employeeId
            )
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping(
        "/challenges/{employeeId}/progress"
    )
    public Map<String, Object> getProgress(
        @PathVariable Long employeeId
    ) {

        return appraisalService
            .getProgress(employeeId);
    }

    private Map<String, Object> convertResponse(
        FormResponse response
    ) {

        Map<String, Object> result =
            new LinkedHashMap<>();

        result.put("id", response.getId());

        result.put(
            "responseType",
            response.getResponseType()
        );

        result.put(
            "phase",
            response.getPhase()
        );

        result.put(
            "questionKey",
            response.getQuestionKey()
        );

        result.put(
            "answer",
            response.getAnswer()
        );

        result.put(
            "challengeName",
            response.getChallengeName()
        );

        result.put(
            "originalFileName",
            response.getOriginalFileName()
        );

        result.put(
            "pointsAwarded",
            response.getPointsAwarded()
        );

        result.put(
            "submittedAt",
            response.getSubmittedAt()
        );

        return result;
    }

    @ExceptionHandler(
        NoSuchElementException.class
    )
    public ResponseEntity<Map<String, String>>
    handleNotFound(
        NoSuchElementException exception
    ) {

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                Map.of(
                    "error",
                    exception.getMessage()
                )
            );
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class
    })
    public ResponseEntity<Map<String, String>>
    handleBadRequest(
        RuntimeException exception
    ) {

        return ResponseEntity
            .badRequest()
            .body(
                Map.of(
                    "error",
                    exception.getMessage()
                )
            );
    }

    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleValidation(
        MethodArgumentNotValidException exception
    ) {

        Map<String, String> fields =
            new LinkedHashMap<>();

        exception
            .getBindingResult()
            .getFieldErrors()
            .forEach(
                error -> fields.put(
                    error.getField(),
                    error.getDefaultMessage()
                )
            );

        return ResponseEntity
            .badRequest()
            .body(
                Map.of(
                    "error",
                    "Validation failed",
                    "fields",
                    fields
                )
            );
    }
}