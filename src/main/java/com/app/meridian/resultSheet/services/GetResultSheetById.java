package com.app.meridian.resultSheet.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetResultSheetById {

    private final ResultSheetRepository resultSheetRepository;

    @Transactional(readOnly = true)
    public ResultSheet get(Long id) {
        ValidationUtils.requireNonNull(id, "ResultSheet ID");
        return resultSheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ResultSheet not found"));
    }
}
