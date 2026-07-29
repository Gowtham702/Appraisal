package com.appraisal.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.appraisal.model.Employee;
import com.appraisal.model.FormResponse;
import com.appraisal.repository.EmployeeRepository;
import com.appraisal.repository.FormRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EmployeeRepository employeeRepository;
    private final FormRepository formRepository;

    public AdminController(
            EmployeeRepository employeeRepository,
            FormRepository formRepository) {

        this.employeeRepository = employeeRepository;
        this.formRepository = formRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {

        List<Employee> employees =
                employeeRepository.findAll();

        long employeeCount =
                employeeRepository.count();

        long answerCount =
                formRepository.count();

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("totalEmployees", employeeCount);
        result.put("totalAnswers", answerCount);
        result.put("employees", employees);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees() {

        return ResponseEntity.ok(
                employeeRepository.findAll()
        );
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<?> getEmployeeDetails(
            @PathVariable Long employeeId) {

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElse(null);

        if (employee == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Employee not found"
                    ));
        }

        List<FormResponse> answers =
                formRepository.findByEmployeeId(
                        employeeId
                );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("employee", employee);
        result.put("answers", answers);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<Map<String, String>> deleteEmployee(
            @PathVariable Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Employee not found"
                    ));
        }

        employeeRepository.deleteById(employeeId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Employee deleted successfully"
                )
        );
    }
}