package com.app.meridian.gradeScale.services;

import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleEvents;
import com.app.meridian.gradeScale.GradeScaleRepository;
import com.app.meridian.gradeScale.dto.GradeScaleRequestDTO;
import com.app.meridian.gradeScale.utils.GradeScaleMapper;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateGradeScale {

    private final GradeScaleRepository gradeScaleRepository;
    private final GradeScaleEvents gradeScaleEvents;
    private final GradeScaleMapper gradeScaleMapper;

    @Transactional
    @CacheEvict(cacheNames = {"gradeScaleByTotal"}, allEntries = true)
    public GradeScale update(Long id, GradeScaleRequestDTO request) {
        ValidationUtils.requireNonNull(id, "GradeScale ID");
        ValidationUtils.requireNonNull(request, "Request body");
        GradeScale entity = gradeScaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GradeScale not found"));
        if (request.grade() != null && !request.grade().isBlank()) {
            if (gradeScaleRepository.existsByGradeIgnoreCaseAndIdNot(request.grade().trim(), id)) {
                throw new ConflictException("GradeScale with this grade already exists");
            }
            entity.setGrade(request.grade().trim());
        }
        if (request.minScore() != null) {
            entity.setMinScore(request.minScore());
        }
        if (request.maxScore() != null) {
            entity.setMaxScore(request.maxScore());
        }
        if (request.remark() != null && !request.remark().isBlank()) {
            entity.setRemark(request.remark().trim());
        }

        GradeScale saved = gradeScaleRepository.save(entity);
        gradeScaleEvents.broadcastUpdated(gradeScaleMapper.toResponse(saved));
        return saved;
    }
}
