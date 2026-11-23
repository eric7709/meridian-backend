package com.app.meridian.leaveRequest.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.leaveRequest.LeaveRequest;
import com.app.meridian.leaveRequest.LeaveRequestEvents;
import com.app.meridian.leaveRequest.LeaveRequestRepository;
import com.app.meridian.leaveRequest.dto.LeaveRequestDTO;
import com.app.meridian.leaveRequest.enums.LeaveStatus;
import com.app.meridian.leaveRequest.utils.LeaveRequestMapper;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestLeave {

    private final EntityResolver entityResolver;
    private final GetCurrentStaff getCurrentStaff;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestEvents leaveRequestEvents;
    private final LeaveRequestMapper leaveRequestMapper;
    private final AuditTrail auditTrail;

    @Transactional
    public LeaveRequest request(LeaveRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.fromDate(), "From date");
        ValidationUtils.requireNonNull(request.toDate(), "To date");
        ValidationUtils.requireNonBlank(request.reason(), "Reason");
        if (request.toDate().isBefore(request.fromDate())) {
            throw new BadRequestException("The leave end date cannot be before the start date");
        }
        Staff staff = request.staffId() == null
                ? getCurrentStaff.get()
                : entityResolver.resolveStaff(request.staffId());
        LeaveRequest leave = new LeaveRequest();
        leave.setStaff(staff);
        leave.setFromDate(request.fromDate());
        leave.setToDate(request.toDate());
        leave.setReason(request.reason().trim());
        leave.setStatus(LeaveStatus.PENDING);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        leaveRequestEvents.broadcastCreated(leaveRequestMapper.toResponse(saved));
        auditTrail.record("LEAVE_REQUESTED", "LeaveRequest", saved.getId(),
                staff.getUser().getFullName() + " requested leave");
        return saved;
    }
}
