package com.app.meridian.leaveRequest.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.leaveRequest.LeaveRequestRepository;
import com.app.meridian.leaveRequest.enums.LeaveStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetStaffOnLeave {

    private final LeaveRequestRepository leaveRequestRepository;

    /** Dashboard count: staff whose approved leave covers today. */
    @Transactional(readOnly = true)
    public long countToday() {
        LocalDate today = LocalDate.now();
        return leaveRequestRepository.findByStatus(LeaveStatus.APPROVED).stream()
                .filter(leave -> !leave.getFromDate().isAfter(today) && !leave.getToDate().isBefore(today))
                .map(leave -> leave.getStaff().getId())
                .distinct()
                .count();
    }
}
