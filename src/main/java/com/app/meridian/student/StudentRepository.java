package com.app.meridian.student;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.student.enums.Gender;
import com.app.meridian.student.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    boolean existsByStudentNoIgnoreCase(String value);
    boolean existsByStudentNoIgnoreCaseAndIdNot(String value, Long id);
    Optional<Student> findByStudentNoIgnoreCase(String value);
    Optional<Student> findByUserId(Long userId);

}
