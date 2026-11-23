package com.app.meridian.attendance;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.attendance.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);
    List<Attendance> findByStudentIdAndTermId(Long studentId, Long termId);
    List<Attendance> findByTermIdAndStudentIdIn(Long termId, List<Long> studentIds);
    Optional<Attendance> findByStudentIdAndDate(Long studentId, java.time.LocalDate date);

}
