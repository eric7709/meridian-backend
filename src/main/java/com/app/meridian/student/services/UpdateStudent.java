package com.app.meridian.student.services;

import java.time.LocalDate;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.student.Student;
import com.app.meridian.student.StudentEvents;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.student.dto.StudentRequestDTO;
import com.app.meridian.student.enums.Gender;
import com.app.meridian.student.enums.StudentStatus;
import com.app.meridian.student.utils.StudentMapper;
import com.app.meridian.user.User;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateStudent {

    private final EntityResolver entityResolver;
    private final StudentRepository studentRepository;
    private final StudentEvents studentEvents;
    private final StudentMapper studentMapper;

    @Transactional
    public Student update(Long id, StudentRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Student ID");
        ValidationUtils.requireNonNull(request, "Request body");
        Student entity = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        if (request.userId() != null) {
            entity.setUser(entityResolver.resolveUser(request.userId()));
        }
        if (request.currentClassId() != null) {
            entity.setCurrentClass(entityResolver.resolveClassRoom(request.currentClassId()));
        }

        if (request.studentNo() != null && !request.studentNo().isBlank()) {
            if (studentRepository.existsByStudentNoIgnoreCaseAndIdNot(request.studentNo().trim(), id)) {
                throw new ConflictException("Student with this studentNo already exists");
            }
            entity.setStudentNo(request.studentNo().trim());
        }
        if (request.gender() != null) {
            entity.setGender(request.gender());
        }
        if (request.dob() != null) {
            entity.setDob(request.dob());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            entity.setPhone(request.phone().trim());
        }
        if (request.guardianName() != null && !request.guardianName().isBlank()) {
            entity.setGuardianName(request.guardianName().trim());
        }
        if (request.guardianPhone() != null && !request.guardianPhone().isBlank()) {
            entity.setGuardianPhone(request.guardianPhone().trim());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        Student saved = studentRepository.save(entity);
        studentEvents.broadcastUpdated(studentMapper.toResponse(saved));
        return saved;
    }
}
