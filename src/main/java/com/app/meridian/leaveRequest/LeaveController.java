package com.app.meridian.leaveRequest;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.leaveRequest.dto.LeaveRequestDTO;
import com.app.meridian.leaveRequest.dto.LeaveRequestResponseDTO;
import com.app.meridian.leaveRequest.dto.LeaveRequestSearchCriteria;
import com.app.meridian.leaveRequest.enums.LeaveStatus;
import com.app.meridian.leaveRequest.services.DecideLeaveRequest;
import com.app.meridian.leaveRequest.services.GetStaffOnLeave;
import com.app.meridian.leaveRequest.services.RequestLeave;
import com.app.meridian.leaveRequest.services.SearchLeaveRequests;
import com.app.meridian.leaveRequest.utils.LeaveRequestMapper;
import com.app.meridian.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian")
public class LeaveController {

    private final RequestLeave requestLeave;
    private final DecideLeaveRequest decideLeaveRequest;
    private final SearchLeaveRequests searchLeaveRequests;
    private final GetStaffOnLeave getStaffOnLeave;
    private final LeaveRequestMapper leaveRequestMapper;

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public LeaveRequestResponseDTO request(@RequestBody LeaveRequestDTO request) {
        return leaveRequestMapper.toResponse(requestLeave.request(request));
    }

    @GetMapping("/leave")
    public PageResponse<LeaveRequestResponseDTO> search(LeaveRequestSearchCriteria criteria) {
        Page<LeaveRequest> page = searchLeaveRequests.search(criteria);
        return PageResponse.of(page, leaveRequestMapper::toResponse);
    }

    @PatchMapping("/leave/{id}/decide")
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveRequestResponseDTO decide(@PathVariable Long id, @RequestParam LeaveStatus status) {
        return leaveRequestMapper.toResponse(decideLeaveRequest.decide(id, status));
    }

    @GetMapping("/staff/on-leave")
    public long onLeave() {
        return getStaffOnLeave.countToday();
    }
}
