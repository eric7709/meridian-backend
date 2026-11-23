package com.app.meridian.score;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScoreRepository extends JpaRepository<Score, Long>, JpaSpecificationExecutor<Score> {

    boolean existsBySheetIdAndStudentId(Long sheetId, Long studentId);
    List<Score> findBySheetId(Long sheetId);
    Optional<Score> findBySheetIdAndStudentId(Long sheetId, Long studentId);
    List<Score> findByStudentId(Long studentId);
    List<Score> findBySheetIdIn(List<Long> sheetIds);

}
