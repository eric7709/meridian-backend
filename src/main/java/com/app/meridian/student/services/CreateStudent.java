package com.app.meridian.student.services;

import java.time.LocalDate;
import java.time.Year;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.exceptions.ConflictException;
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

@Service
@RequiredArgsConstructor
public class CreateStudent {

    private final EntityResolver entityResolver;
    private final StudentRepository studentRepository;
    private final StudentEvents studentEvents;
    private final StudentMapper studentMapper;

    @Transactional
    public Student create(StudentRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.userId(), "user");

        User user = entityResolver.resolveUser(request.userId());
        ClassRoom currentClass = entityResolver.resolveClassRoom(request.currentClassId());

        if (request.studentNo() != null && studentRepository.existsByStudentNoIgnoreCase(request.studentNo().trim())) {
            throw new ConflictException("Student with this studentNo already exists");
        }

        Student entity = new Student();
        String studentNo = request.studentNo() == null || request.studentNo().isBlank()
                ? nextStudentNo()
                : request.studentNo().trim();
        entity.setStudentNo(studentNo);
        if (request.gender() != null) {
            entity.setGender(request.gender());
        }
        if (request.dob() != null) {
            entity.setDob(request.dob());
        }
        entity.setPhone(ValidationUtils.trimOrNull(request.phone()));
        entity.setGuardianName(ValidationUtils.trimOrNull(request.guardianName()));
        entity.setGuardianPhone(ValidationUtils.trimOrNull(request.guardianPhone()));
        entity.setStatus(request.status() == null ? StudentStatus.ACTIVE : request.status());
        entity.setUser(user);
        entity.setCurrentClass(currentClass);

        Student saved = studentRepository.save(entity);
        studentEvents.broadcastCreated(studentMapper.toResponse(saved));
        return saved;
    }

    private String nextStudentNo() {
        long sequence = studentRepository.count() + 1;
        String candidate;
        do {
            candidate = "STU/%d/%03d".formatted(Year.now().getValue(), sequence++);
        } while (studentRepository.existsByStudentNoIgnoreCase(candidate));
        return candidate;
    }
}
