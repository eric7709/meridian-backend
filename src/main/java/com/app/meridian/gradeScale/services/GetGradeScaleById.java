package com.app.meridian.gradeScale.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetGradeScaleById {

    private final GradeScaleRepository gradeScaleRepository;

    @Transactional(readOnly = true)
    public GradeScale get(Long id) {
        ValidationUtils.requireNonNull(id, "GradeScale ID");
        return gradeScaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GradeScale not found"));
    }
}
