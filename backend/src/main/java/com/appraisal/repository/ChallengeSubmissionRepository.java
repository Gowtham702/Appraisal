package com.appraisal.repository;

import java.util.List;

import com.appraisal.model.ChallengeSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeSubmissionRepository
        extends JpaRepository<ChallengeSubmission, Long> {

    List<ChallengeSubmission> findByEmployee_Id(
            Long employeeId
    );
}