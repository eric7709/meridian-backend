package com.app.meridian.subject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.subject.enums.SubjectLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    boolean existsByCodeIgnoreCase(String value);
    boolean existsByCodeIgnoreCaseAndIdNot(String value, Long id);
    Optional<Subject> findByCodeIgnoreCase(String value);

}
