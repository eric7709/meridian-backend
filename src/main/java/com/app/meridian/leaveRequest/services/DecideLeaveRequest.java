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
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DecideLeaveRequest {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestEvents leaveRequestEvents;
    private final LeaveRequestMapper leaveRequestMapper;
    private final AuditTrail auditTrail;

    @Transactional
    public LeaveRequest decide(Long id, LeaveStatus decision) {
        ValidationUtils.requireNonNull(id, "Leave request ID");
        ValidationUtils.requireNonNull(decision, "Decision");
        if (decision == LeaveStatus.PENDING) {
            throw new BadRequestException("Choose approved or rejected");
        }
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Leave request not found"));
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("This request has already been decided");
        }
        leave.setStatus(decision);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        leaveRequestEvents.broadcastUpdated(leaveRequestMapper.toResponse(saved));
        auditTrail.record("LEAVE_" + decision.name(), "LeaveRequest", saved.getId(),
                "Leave " + decision.name().toLowerCase() + " for " + leave.getStaff().getUser().getFullName());
        return saved;
    }
}
