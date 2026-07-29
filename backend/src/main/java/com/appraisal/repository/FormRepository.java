package com.appraisal.repository;

import com.appraisal.model.FormResponse;
import com.appraisal.model.FormResponse.ResponseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FormRepository
        extends JpaRepository<FormResponse, Long> {

    List<FormResponse> findByEmployeeId(Long employeeId);

    List<FormResponse>
    findByEmployeeIdOrderBySubmittedAtAsc(Long employeeId);

    long countByEmployeeIdAndResponseType(
        Long employeeId,
        ResponseType responseType
    );

    boolean existsByEmployeeIdAndResponseTypeAndChallengeName(
        Long employeeId,
        ResponseType responseType,
        String challengeName
    );
}