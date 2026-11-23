package com.app.meridian.schoolSettings.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.schoolSettings.SchoolSettingsRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSchoolSettingsById {

    private final SchoolSettingsRepository schoolSettingsRepository;

    @Transactional(readOnly = true)
    public SchoolSettings get(Long id) {
        ValidationUtils.requireNonNull(id, "SchoolSettings ID");
        return schoolSettingsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SchoolSettings not found"));
    }
}
