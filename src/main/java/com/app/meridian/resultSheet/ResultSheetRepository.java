package com.app.meridian.resultSheet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResultSheetRepository extends JpaRepository<ResultSheet, Long>, JpaSpecificationExecutor<ResultSheet> {

    boolean existsByCodeIgnoreCase(String value);
    boolean existsByCodeIgnoreCaseAndIdNot(String value, Long id);
    Optional<ResultSheet> findByCodeIgnoreCase(String value);
    boolean existsByAssignmentIdAndTermId(Long assignmentId, Long termId);
    Optional<ResultSheet> findByAssignmentIdAndTermId(Long assignmentId, Long termId);
    List<ResultSheet> findByAssignmentTeacherId(Long teacherId);
    List<ResultSheet> findByAssignmentTeacherIdAndTermId(Long teacherId, Long termId);
    long countByStatus(ResultSheetStatus status);
    List<ResultSheet> findByAssignmentClassRoomIdAndTermIdAndStatus(Long classId, Long termId, ResultSheetStatus status);

}
