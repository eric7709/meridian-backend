package com.app.meridian.gradeScale;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GradeScaleRepository extends JpaRepository<GradeScale, Long>, JpaSpecificationExecutor<GradeScale> {

    boolean existsByGradeIgnoreCase(String value);
    boolean existsByGradeIgnoreCaseAndIdNot(String value, Long id);
    Optional<GradeScale> findByGradeIgnoreCase(String value);

}
