package com.app.meridian.gradeScale.services;

import com.app.meridian.exceptions.ConflictException;
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

@Service
@RequiredArgsConstructor
public class CreateGradeScale {

    private final GradeScaleRepository gradeScaleRepository;
    private final GradeScaleEvents gradeScaleEvents;
    private final GradeScaleMapper gradeScaleMapper;

    @Transactional
    @CacheEvict(cacheNames = {"gradeScaleByTotal"}, allEntries = true)
    public GradeScale create(GradeScaleRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.grade(), "grade");
        ValidationUtils.requireNonNull(request.minScore(), "minScore");
        ValidationUtils.requireNonNull(request.maxScore(), "maxScore");
        ValidationUtils.requireNonBlank(request.remark(), "remark");

        if (request.grade() != null && gradeScaleRepository.existsByGradeIgnoreCase(request.grade().trim())) {
            throw new ConflictException("GradeScale with this grade already exists");
        }

        GradeScale entity = new GradeScale();
        entity.setGrade(request.grade().trim());
        entity.setMinScore(request.minScore());
        entity.setMaxScore(request.maxScore());
        entity.setRemark(request.remark().trim());

        GradeScale saved = gradeScaleRepository.save(entity);
        gradeScaleEvents.broadcastCreated(gradeScaleMapper.toResponse(saved));
        return saved;
    }
}
