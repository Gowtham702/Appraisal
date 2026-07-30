package com.appraisal.repository;

import com.appraisal.entity.WeeklyChallengeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyChallengeRepository
        extends JpaRepository<WeeklyChallengeAssignment, Long> {

    Optional<WeeklyChallengeAssignment>
        findByEmployeeIdAndWeekStart(Long employeeId, LocalDate weekStart);

    List<WeeklyChallengeAssignment>
        findByEmployeeIdAndCompletedTrue(Long employeeId);

    List<WeeklyChallengeAssignment>
        findByEmployeeIdOrderByWeekStartDesc(Long employeeId);
}