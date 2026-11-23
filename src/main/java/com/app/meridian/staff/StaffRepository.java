package com.app.meridian.staff;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.staff.enums.EmploymentType;
import com.app.meridian.staff.enums.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StaffRepository extends JpaRepository<Staff, Long>, JpaSpecificationExecutor<Staff> {

    boolean existsByStaffNoIgnoreCase(String value);
    boolean existsByStaffNoIgnoreCaseAndIdNot(String value, Long id);
    Optional<Staff> findByStaffNoIgnoreCase(String value);
    Optional<Staff> findByUserId(Long userId);
    List<Staff> findByStaffRole(StaffRole staffRole);

}
