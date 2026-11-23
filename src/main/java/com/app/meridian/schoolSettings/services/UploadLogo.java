package com.app.meridian.schoolSettings.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

/** JPG or PNG, max 2MB — the same rule the General settings tab shows. */
@Service
@RequiredArgsConstructor
public class UploadLogo {

    private final UpdateSchoolSettings updateSchoolSettings;

    @Value("${meridian.uploads.directory:uploads}")
    private String uploadsDirectory;

    @Transactional
    public SchoolSettings upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a JPG or PNG file");
        }
        if (file.getSize() > 2L * 1024 * 1024) {
            throw new BadRequestException("The logo must be 2MB or smaller");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String extension = original.endsWith(".png") ? ".png" : original.endsWith(".jpg") || original.endsWith(".jpeg") ? ".jpg" : null;
        if (extension == null) {
            throw new BadRequestException("The logo must be a JPG or PNG image");
        }
        try {
            Path directory = Paths.get(uploadsDirectory);
            Files.createDirectories(directory);
            String filename = "logo-" + UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return updateSchoolSettings.setLogo("/uploads/" + filename);
        } catch (IOException ex) {
            throw new BadRequestException("The logo could not be stored");
        }
    }
}
