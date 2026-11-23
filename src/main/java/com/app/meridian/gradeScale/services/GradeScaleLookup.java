package com.app.meridian.gradeScale.services;

import java.util.Comparator;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.app.meridian.cache.CacheNames;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleRepository;

import lombok.RequiredArgsConstructor;

/**
 * Grade and remark always come from the grade scale table — never typed in by a teacher. Called on
 * every score save, so the lookup by total is cached; grade-scale edits evict the whole cache.
 */
@Component
@RequiredArgsConstructor
public class GradeScaleLookup {

    private final GradeScaleRepository gradeScaleRepository;

    @Cacheable(cacheNames = CacheNames.GRADE_SCALE_BY_TOTAL, key = "#total")
    public GradeScale forTotal(Double total) {
        if (total == null) {
            return null;
        }
        return gradeScaleRepository.findAll().stream()
                .filter(scale -> total >= scale.getMinScore() && total <= scale.getMaxScore())
                .min(Comparator.comparing(GradeScale::getMinScore))
                .orElse(null);
    }
}
