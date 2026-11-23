package com.app.meridian.teachingAssignment;

import com.app.meridian.common.PageResponse;
import com.app.meridian.teachingAssignment.dto.TeachingAssignmentRequestDTO;
import com.app.meridian.teachingAssignment.dto.TeachingAssignmentResponseDTO;
import com.app.meridian.teachingAssignment.dto.TeachingAssignmentSearchCriteria;
import com.app.meridian.teachingAssignment.services.CreateTeachingAssignment;
import com.app.meridian.teachingAssignment.services.DeleteTeachingAssignment;
import com.app.meridian.teachingAssignment.services.GetTeachingAssignmentById;
import com.app.meridian.teachingAssignment.services.SearchTeachingAssignments;
import com.app.meridian.teachingAssignment.services.UpdateTeachingAssignment;
import com.app.meridian.teachingAssignment.utils.TeachingAssignmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/assignments")
public class TeachingAssignmentController {

    private final CreateTeachingAssignment createTeachingAssignment;
    private final UpdateTeachingAssignment updateTeachingAssignment;
    private final DeleteTeachingAssignment deleteTeachingAssignment;
    private final GetTeachingAssignmentById getTeachingAssignmentById;
    private final SearchTeachingAssignments searchTeachingAssignments;
    private final TeachingAssignmentMapper teachingAssignmentMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public TeachingAssignmentResponseDTO create(@RequestBody TeachingAssignmentRequestDTO request) {
        return teachingAssignmentMapper.toResponse(createTeachingAssignment.create(request));
    }

    @GetMapping
    public PageResponse<TeachingAssignmentResponseDTO> search(TeachingAssignmentSearchCriteria criteria) {
        Page<TeachingAssignment> page = searchTeachingAssignments.search(criteria);
        return PageResponse.of(page, teachingAssignmentMapper::toResponse);
    }

    @GetMapping("/{id}")
    public TeachingAssignmentResponseDTO get(@PathVariable Long id) {
        return teachingAssignmentMapper.toResponse(getTeachingAssignmentById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public TeachingAssignmentResponseDTO update(@PathVariable Long id, @RequestBody TeachingAssignmentRequestDTO request) {
        return teachingAssignmentMapper.toResponse(updateTeachingAssignment.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteTeachingAssignment.delete(id);
    }
}
