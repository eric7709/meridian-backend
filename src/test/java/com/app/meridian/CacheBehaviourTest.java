package com.app.meridian;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.app.meridian.cache.CacheNames;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleRepository;
import com.app.meridian.gradeScale.dto.GradeScaleRequestDTO;
import com.app.meridian.gradeScale.services.GradeScaleLookup;
import com.app.meridian.gradeScale.services.UpdateGradeScale;
import com.app.meridian.schoolSettings.dto.ModulesRequestDTO;
import com.app.meridian.schoolSettings.services.GetSchoolSettings;
import com.app.meridian.schoolSettings.services.UpdateSchoolSettings;

/**
 * Caching is only correct if it is invalidated: every test reads through a cache, writes through the
 * service, and then proves the stale entry is gone.
 */
@SpringBootTest
class CacheBehaviourTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GetSchoolSettings getSchoolSettings;

    @Autowired
    private UpdateSchoolSettings updateSchoolSettings;

    @Autowired
    private GradeScaleLookup gradeScaleLookup;

    @Autowired
    private UpdateGradeScale updateGradeScale;

    @Autowired
    private GradeScaleRepository gradeScaleRepository;

    @Test
    void settingsAreCachedAndEvictedWhenTheyChange() {
        Cache settings = cacheManager.getCache(CacheNames.SCHOOL_SETTINGS);
        assertThat(settings).isNotNull();
        settings.clear();

        getSchoolSettings.get();
        assertThat(settings.get("current")).as("first read populates the cache").isNotNull();

        updateSchoolSettings.updateModules(new ModulesRequestDTO(true, true));
        assertThat(settings.get("current")).as("a module toggle must not leave a stale row").isNull();

        getSchoolSettings.get();
        assertThat(settings.get("current")).isNotNull();
    }

    @Test
    void gradeLookupIsCachedAndEvictedWhenTheScaleChanges() {
        Cache grades = cacheManager.getCache(CacheNames.GRADE_SCALE_BY_TOTAL);
        assertThat(grades).isNotNull();
        grades.clear();

        GradeScale scale = gradeScaleLookup.forTotal(80.0);
        assertThat(scale).isNotNull();
        assertThat(grades.get(80.0)).isNotNull();

        GradeScale existing = gradeScaleRepository.findAll().get(0);
        updateGradeScale.update(existing.getId(),
                new GradeScaleRequestDTO(existing.getGrade(), existing.getMinScore(), existing.getMaxScore(),
                        existing.getRemark()));
        assertThat(grades.get(80.0)).as("editing the scale invalidates the lookup").isNull();
    }
}
