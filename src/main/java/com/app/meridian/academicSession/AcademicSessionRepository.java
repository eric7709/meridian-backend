package com.app.meridian.academicSession;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long>, JpaSpecificationExecutor<AcademicSession> {

    boolean existsByLabelIgnoreCase(String value);
    boolean existsByLabelIgnoreCaseAndIdNot(String value, Long id);
    Optional<AcademicSession> findByLabelIgnoreCase(String value);
    Optional<AcademicSession> findByIsCurrentTrue();

}
