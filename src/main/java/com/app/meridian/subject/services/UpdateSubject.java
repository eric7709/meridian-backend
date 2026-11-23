package com.app.meridian.subject.services;

import com.app.meridian.department.Department;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
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

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateSubject {

    private final EntityResolver entityResolver;
    private final SubjectRepository subjectRepository;
    private final SubjectEvents subjectEvents;
    private final SubjectMapper subjectMapper;

    @Transactional
    public Subject update(Long id, SubjectRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Subject ID");
        ValidationUtils.requireNonNull(request, "Request body");
        Subject entity = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject not found"));
        if (request.departmentId() != null) {
            entity.setDepartment(entityResolver.resolveDepartment(request.departmentId()));
        }

        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name().trim());
        }
        if (request.code() != null && !request.code().isBlank()) {
            if (subjectRepository.existsByCodeIgnoreCaseAndIdNot(request.code().trim(), id)) {
                throw new ConflictException("Subject with this code already exists");
            }
            entity.setCode(request.code().trim());
        }
        if (request.level() != null) {
            entity.setLevel(request.level());
        }

        Subject saved = subjectRepository.save(entity);
        subjectEvents.broadcastUpdated(subjectMapper.toResponse(saved));
        return saved;
    }
}
