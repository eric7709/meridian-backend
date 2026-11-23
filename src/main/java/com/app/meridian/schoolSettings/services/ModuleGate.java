package com.app.meridian.schoolSettings.services;

import org.springframework.stereotype.Component;

import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Optional modules: when a module is switched off its endpoints answer 404 and the frontend hides
 * the card, exactly as the settings screen promises.
 */
@Component
@RequiredArgsConstructor
public class ModuleGate {

    private final GetSchoolSettings getSchoolSettings;

    public void requireDepartments() {
        if (!Boolean.TRUE.equals(getSchoolSettings.get().getDepartmentsEnabled())) {
            throw new NotFoundException("The departments module is switched off");
        }
    }

    public void requireFees() {
        SchoolSettings settings = getSchoolSettings.get();
        if (!Boolean.TRUE.equals(settings.getFeesEnabled())) {
            throw new NotFoundException("The fees module is switched off");
        }
    }
}
