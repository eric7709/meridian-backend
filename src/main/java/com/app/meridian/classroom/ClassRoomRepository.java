package com.app.meridian.classroom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.classroom.enums.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long>, JpaSpecificationExecutor<ClassRoom> {

    boolean existsByNameIgnoreCase(String value);
    boolean existsByNameIgnoreCaseAndIdNot(String value, Long id);
    Optional<ClassRoom> findByNameIgnoreCase(String value);

}
