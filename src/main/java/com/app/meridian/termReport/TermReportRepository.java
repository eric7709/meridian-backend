package com.app.meridian.termReport;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TermReportRepository extends JpaRepository<TermReport, Long>, JpaSpecificationExecutor<TermReport> {

    boolean existsByStudentIdAndTermId(Long studentId, Long termId);
    Optional<TermReport> findByStudentIdAndTermId(Long studentId, Long termId);
    List<TermReport> findByTermId(Long termId);
    List<TermReport> findByStudentId(Long studentId);

}
