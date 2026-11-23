package com.app.meridian.leaveRequest.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.leaveRequest.LeaveRequest;
import com.app.meridian.leaveRequest.LeaveRequestRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLeaveRequestById {

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public LeaveRequest get(Long id) {
        ValidationUtils.requireNonNull(id, "LeaveRequest ID");
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LeaveRequest not found"));
    }
}
