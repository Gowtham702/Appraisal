package com.appraisal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "employees",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_employee_code",
            columnNames = "employee_code"
        )
    }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee name is required")
    @Pattern(
        regexp = "^[A-Za-z ]+$",
        message = "Employee name must contain only letters and spaces"
    )
    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @NotBlank(message = "Employee ID is required")
    @Pattern(
        regexp = "^[0-9]+$",
        message = "Employee ID must contain only numbers"
    )
    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Pattern(
        regexp = "^[A-Za-z ]*$",
        message = "Department must contain only letters and spaces"
    )
    private String department;

    @Pattern(
        regexp = "^[A-Za-z ]*$",
        message = "Designation must contain only letters and spaces"
    )
    private String designation;

    @Pattern(
        regexp = "^[A-Za-z ]*$",
        message = "Current role must contain only letters and spaces"
    )
    @Column(name = "current_role")
    private String currentRole;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}