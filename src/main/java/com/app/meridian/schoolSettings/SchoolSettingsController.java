package com.app.meridian.schoolSettings;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.meridian.schoolSettings.dto.ModulesRequestDTO;
import com.app.meridian.schoolSettings.dto.SchoolSettingsRequestDTO;
import com.app.meridian.schoolSettings.dto.SchoolSettingsResponseDTO;
import com.app.meridian.schoolSettings.services.GetSchoolSettings;
import com.app.meridian.schoolSettings.services.UpdateSchoolSettings;
import com.app.meridian.schoolSettings.services.UploadLogo;
import com.app.meridian.schoolSettings.utils.SchoolSettingsMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/settings")
public class SchoolSettingsController {

    private final GetSchoolSettings getSchoolSettings;
    private final UpdateSchoolSettings updateSchoolSettings;
    private final UploadLogo uploadLogo;
    private final SchoolSettingsMapper schoolSettingsMapper;

    @GetMapping
    public SchoolSettingsResponseDTO get() {
        return schoolSettingsMapper.toResponse(getSchoolSettings.get());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SchoolSettingsResponseDTO update(@RequestBody SchoolSettingsRequestDTO request) {
        return schoolSettingsMapper.toResponse(updateSchoolSettings.update(request));
    }

    @PutMapping("/academic")
    @PreAuthorize("hasRole('ADMIN')")
    public SchoolSettingsResponseDTO academic(@RequestBody SchoolSettingsRequestDTO request) {
        return schoolSettingsMapper.toResponse(updateSchoolSettings.update(request));
    }

    @PutMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public SchoolSettingsResponseDTO modules(@RequestBody ModulesRequestDTO request) {
        return schoolSettingsMapper.toResponse(updateSchoolSettings.updateModules(request));
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public SchoolSettingsResponseDTO logo(@RequestParam("file") MultipartFile file) {
        return schoolSettingsMapper.toResponse(uploadLogo.upload(file));
    }
}
