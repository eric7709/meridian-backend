package com.app.meridian.teachingAssignment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long>, JpaSpecificationExecutor<TeachingAssignment> {

    boolean existsByTeacherIdAndSubjectIdAndClassRoomIdAndSessionId(Long teacherId, Long subjectId, Long classRoomId, Long sessionId);
    List<TeachingAssignment> findByTeacherIdAndSessionId(Long teacherId, Long sessionId);
    List<TeachingAssignment> findByTeacherId(Long teacherId);
    List<TeachingAssignment> findBySessionId(Long sessionId);

}
