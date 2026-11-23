package com.app.meridian.gradeScale.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.gradeScale.GradeScaleEvents;
import com.app.meridian.gradeScale.GradeScaleRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteGradeScale {

    private final GradeScaleRepository gradeScaleRepository;
    private final GradeScaleEvents gradeScaleEvents;

    @Transactional
    @CacheEvict(cacheNames = {"gradeScaleByTotal"}, allEntries = true)
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "GradeScale ID");
        if (!gradeScaleRepository.existsById(id)) {
            throw new NotFoundException("GradeScale not found");
        }
        gradeScaleRepository.deleteById(id);
        gradeScaleEvents.broadcastDeleted(id);
    }
}
