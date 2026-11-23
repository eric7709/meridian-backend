package com.app.meridian.leaveRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.leaveRequest.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    List<LeaveRequest> findByStatus(LeaveStatus status);
    long countByStatus(LeaveStatus status);

}
