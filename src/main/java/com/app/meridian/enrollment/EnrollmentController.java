package com.app.meridian.enrollment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.enrollment.dto.EnrollmentRequestDTO;
import com.app.meridian.enrollment.dto.EnrollmentResponseDTO;
import com.app.meridian.enrollment.dto.EnrollmentSearchCriteria;
import com.app.meridian.enrollment.dto.PromoteRequestDTO;
import com.app.meridian.enrollment.services.EnrollStudent;
import com.app.meridian.enrollment.services.PromoteStudents;
import com.app.meridian.enrollment.services.SearchEnrollments;
import com.app.meridian.enrollment.utils.EnrollmentMapper;
import com.app.meridian.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/enrollments")
public class EnrollmentController {

    private final EnrollStudent enrollStudent;
    private final PromoteStudents promoteStudents;
    private final SearchEnrollments searchEnrollments;
    private final EnrollmentMapper enrollmentMapper;

    @GetMapping
    public PageResponse<EnrollmentResponseDTO> search(EnrollmentSearchCriteria criteria) {
        Page<Enrollment> page = searchEnrollments.search(criteria);
        return PageResponse.of(page, enrollmentMapper::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public EnrollmentResponseDTO enroll(@RequestBody EnrollmentRequestDTO request) {
        return enrollmentMapper.toResponse(enrollStudent.enroll(request));
    }

    @PostMapping("/promote")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public List<Long> promote(@RequestBody PromoteRequestDTO request) {
        return promoteStudents.promote(request);
    }
}
