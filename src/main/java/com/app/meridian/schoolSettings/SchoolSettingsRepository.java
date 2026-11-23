package com.app.meridian.schoolSettings;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long>, JpaSpecificationExecutor<SchoolSettings> {

    Optional<SchoolSettings> findFirstByOrderByIdAsc();

}
