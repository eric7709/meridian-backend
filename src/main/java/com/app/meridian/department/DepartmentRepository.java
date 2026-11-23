package com.app.meridian.department;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    boolean existsByNameIgnoreCase(String value);
    boolean existsByNameIgnoreCaseAndIdNot(String value, Long id);
    Optional<Department> findByNameIgnoreCase(String value);

}
