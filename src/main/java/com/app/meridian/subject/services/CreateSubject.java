package com.app.meridian.subject.services;

import java.time.Year;
import com.app.meridian.department.Department;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.subject.Subject;
import com.app.meridian.subject.SubjectEvents;
import com.app.meridian.subject.SubjectRepository;
import com.app.meridian.subject.dto.SubjectRequestDTO;
import com.app.meridian.subject.enums.SubjectLevel;
import com.app.meridian.subject.utils.SubjectMapper;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSubject {

    private final EntityResolver entityResolver;
    private final SubjectRepository subjectRepository;
    private final SubjectEvents subjectEvents;
    private final SubjectMapper subjectMapper;

    @Transactional
    public Subject create(SubjectRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.name(), "name");

        Department department = entityResolver.resolveDepartment(request.departmentId());

        if (request.code() != null && subjectRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("Subject with this code already exists");
        }

        Subject entity = new Subject();
        String code = request.code() == null || request.code().isBlank()
                ? nextCode()
                : request.code().trim();
        entity.setCode(code);
        entity.setName(request.name().trim());
        entity.setLevel(request.level() == null ? SubjectLevel.ALL : request.level());
        entity.setDepartment(department);

        Subject saved = subjectRepository.save(entity);
        subjectEvents.broadcastCreated(subjectMapper.toResponse(saved));
        return saved;
    }

    private String nextCode() {
        long sequence = subjectRepository.count() + 1;
        String candidate;
        do {
            candidate = "SUB/%d/%03d".formatted(Year.now().getValue(), sequence++);
        } while (subjectRepository.existsByCodeIgnoreCase(candidate));
        return candidate;
    }
}
