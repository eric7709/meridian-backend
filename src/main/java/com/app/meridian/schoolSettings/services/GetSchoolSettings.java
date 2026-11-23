package com.app.meridian.schoolSettings.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.schoolSettings.SchoolSettingsRepository;
import com.app.meridian.cache.CacheNames;

import lombok.RequiredArgsConstructor;

/** The settings table holds exactly one row; it is created on first read. */
@Service
@RequiredArgsConstructor
public class GetSchoolSettings {

    private final SchoolSettingsRepository schoolSettingsRepository;

    @Cacheable(cacheNames = CacheNames.SCHOOL_SETTINGS, key = "'current'")
    @Transactional
    public SchoolSettings get() {
        return schoolSettingsRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            SchoolSettings settings = new SchoolSettings();
            settings.setName("Meridian");
            settings.setDepartmentsEnabled(Boolean.TRUE);
            settings.setFeesEnabled(Boolean.TRUE);
            return schoolSettingsRepository.save(settings);
        });
    }
}
