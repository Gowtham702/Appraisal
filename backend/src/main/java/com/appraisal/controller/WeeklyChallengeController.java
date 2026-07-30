package com.appraisal.controller;

import com.appraisal.dto.ChallengeStatusResponse;
import com.appraisal.dto.SpinResponse;
import com.appraisal.dto.SubmissionResponse;
import com.appraisal.service.WeeklyChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/weekly-challenges")
public class WeeklyChallengeController {

    private final WeeklyChallengeService service;

    public WeeklyChallengeController(
            WeeklyChallengeService service) {
        this.service = service;
    }

    @GetMapping("/{employeeId}/status")
    public ResponseEntity<ChallengeStatusResponse> getStatus(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                service.getStatus(employeeId)
        );
    }

    @PostMapping("/{employeeId}/spin")
    public ResponseEntity<SpinResponse> spin(
            @PathVariable Long employeeId) {

        SpinResponse response = service.spin(employeeId);

        if (!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(
        value = "/{employeeId}/submit",
        consumes = "multipart/form-data"
    )
    public ResponseEntity<?> submit(
            @PathVariable Long employeeId,
            @RequestParam("file") MultipartFile file) {

        try {
            return ResponseEntity.ok(
                    service.submit(employeeId, file)
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 SecurityException exception) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                        "success", false,
                        "message", exception.getMessage()
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                        "success", false,
                        "message",
                        "Unable to upload the file."
                    )
            );
        }
    }
}