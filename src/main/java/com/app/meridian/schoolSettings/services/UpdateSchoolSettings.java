package com.app.meridian.schoolSettings.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.schoolSettings.SchoolSettingsEvents;
import com.app.meridian.schoolSettings.SchoolSettingsRepository;
import com.app.meridian.schoolSettings.dto.ModulesRequestDTO;
import com.app.meridian.schoolSettings.dto.SchoolSettingsRequestDTO;
import com.app.meridian.cache.CacheNames;
import com.app.meridian.schoolSettings.utils.SchoolSettingsMapper;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateSchoolSettings {

    private final GetSchoolSettings getSchoolSettings;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final SchoolSettingsEvents schoolSettingsEvents;
    private final SchoolSettingsMapper schoolSettingsMapper;
    private final EntityResolver entityResolver;
    private final AuditTrail auditTrail;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.SCHOOL_SETTINGS, allEntries = true)
    public SchoolSettings update(SchoolSettingsRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        SchoolSettings settings = getSchoolSettings.get();
        if (request.name() != null && !request.name().isBlank()) {
            settings.setName(request.name().trim());
        }
        if (request.email() != null) {
            settings.setEmail(ValidationUtils.trimOrNull(request.email()));
        }
        if (request.phone() != null) {
            settings.setPhone(ValidationUtils.trimOrNull(request.phone()));
        }
        if (request.website() != null) {
            settings.setWebsite(ValidationUtils.trimOrNull(request.website()));
        }
        if (request.address() != null) {
            settings.setAddress(ValidationUtils.trimOrNull(request.address()));
        }
        if (request.logoUrl() != null) {
            settings.setLogoUrl(ValidationUtils.trimOrNull(request.logoUrl()));
        }
        if (request.notificationPrefs() != null) {
            settings.setNotificationPrefs(ValidationUtils.trimOrNull(request.notificationPrefs()));
        }
        if (request.departmentsEnabled() != null) {
            settings.setDepartmentsEnabled(request.departmentsEnabled());
        }
        if (request.feesEnabled() != null) {
            settings.setFeesEnabled(request.feesEnabled());
        }
        if (request.currentTermId() != null) {
            settings.setCurrentTerm(entityResolver.resolveTerm(request.currentTermId()));
        }
        SchoolSettings saved = schoolSettingsRepository.save(settings);
        schoolSettingsEvents.broadcastUpdated(schoolSettingsMapper.toResponse(saved));
        auditTrail.record("SETTINGS_UPDATED", "SchoolSettings", saved.getId(), "School settings updated");
        return saved;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.SCHOOL_SETTINGS, allEntries = true)
    public SchoolSettings updateModules(ModulesRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        SchoolSettings settings = getSchoolSettings.get();
        if (request.departmentsEnabled() != null) {
            settings.setDepartmentsEnabled(request.departmentsEnabled());
        }
        if (request.feesEnabled() != null) {
            settings.setFeesEnabled(request.feesEnabled());
        }
        SchoolSettings saved = schoolSettingsRepository.save(settings);
        schoolSettingsEvents.broadcastUpdated(schoolSettingsMapper.toResponse(saved));
        auditTrail.record("MODULES_UPDATED", "SchoolSettings", saved.getId(),
                "departments=" + saved.getDepartmentsEnabled() + ", fees=" + saved.getFeesEnabled());
        return saved;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.SCHOOL_SETTINGS, allEntries = true)
    public SchoolSettings setLogo(String logoUrl) {
        SchoolSettings settings = getSchoolSettings.get();
        if (logoUrl == null || logoUrl.isBlank()) {
            throw new NotFoundException("Upload a JPG or PNG file first");
        }
        settings.setLogoUrl(logoUrl.trim());
        return schoolSettingsRepository.save(settings);
    }
}
