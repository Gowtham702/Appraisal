package com.appraisal.repository;

import java.util.Optional;

import com.appraisal.model.Employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(
            String employeeCode
    );

    boolean existsByEmployeeCode(
            String employeeCode
    );
}