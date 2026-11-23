package com.app.meridian.enrollment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {

    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);
    List<Enrollment> findByStudentIdOrderByEnrolledOnDesc(Long studentId);
    List<Enrollment> findByClassRoomIdAndSessionId(Long classId, Long sessionId);
    Optional<Enrollment> findByStudentIdAndSessionId(Long studentId, Long sessionId);
    List<Enrollment> findBySessionId(Long sessionId);

}
