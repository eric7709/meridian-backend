package com.app.meridian.attendance.services;

import com.app.meridian.attendance.Attendance;
import com.app.meridian.attendance.AttendanceRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAttendanceById {

    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public Attendance get(Long id) {
        ValidationUtils.requireNonNull(id, "Attendance ID");
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));
    }
}
