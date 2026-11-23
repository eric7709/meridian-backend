package com.app.meridian.attendance;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.attendance.dto.AttendanceRegisterRequestDTO;
import com.app.meridian.attendance.dto.AttendanceResponseDTO;
import com.app.meridian.attendance.dto.AttendanceSummaryDTO;
import com.app.meridian.attendance.services.GetAttendanceSummary;
import com.app.meridian.attendance.services.MarkAttendanceRegister;
import com.app.meridian.attendance.dto.AttendanceSearchCriteria;
import com.app.meridian.attendance.services.SearchAttendanceRecords;
import com.app.meridian.attendance.utils.AttendanceMapper;
import com.app.meridian.common.PageResponse;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/attendance")
public class AttendanceController {

    private final MarkAttendanceRegister markAttendanceRegister;
    private final GetAttendanceSummary getAttendanceSummary;
    private final SearchAttendanceRecords searchAttendanceRecords;
    private final AttendanceMapper attendanceMapper;

    /** Every register row, filterable by student, term, date range and status. */
    @GetMapping
    public PageResponse<AttendanceResponseDTO> search(AttendanceSearchCriteria criteria) {
        Page<Attendance> page = searchAttendanceRecords.search(criteria);
        return PageResponse.of(page, attendanceMapper::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public List<AttendanceResponseDTO> mark(@RequestBody AttendanceRegisterRequestDTO request) {
        return markAttendanceRegister.mark(request).stream().map(attendanceMapper::toResponse).toList();
    }

    @GetMapping("/summary")
    public AttendanceSummaryDTO summary(@RequestParam Long termId, @RequestParam Long classId) {
        return getAttendanceSummary.forClass(termId, classId);
    }
}
