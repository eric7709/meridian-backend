package com.app.meridian.subject;

import com.app.meridian.common.PageResponse;
import com.app.meridian.subject.dto.SubjectRequestDTO;
import com.app.meridian.subject.dto.SubjectResponseDTO;
import com.app.meridian.subject.dto.SubjectSearchCriteria;
import com.app.meridian.subject.services.CreateSubject;
import com.app.meridian.subject.services.DeleteSubject;
import com.app.meridian.subject.services.GetSubjectById;
import com.app.meridian.subject.services.SearchSubjects;
import com.app.meridian.subject.services.UpdateSubject;
import com.app.meridian.subject.utils.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/subjects")
public class SubjectController {

    private final CreateSubject createSubject;
    private final UpdateSubject updateSubject;
    private final DeleteSubject deleteSubject;
    private final GetSubjectById getSubjectById;
    private final SearchSubjects searchSubjects;
    private final SubjectMapper subjectMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public SubjectResponseDTO create(@RequestBody SubjectRequestDTO request) {
        return subjectMapper.toResponse(createSubject.create(request));
    }

    @GetMapping
    public PageResponse<SubjectResponseDTO> search(SubjectSearchCriteria criteria) {
        Page<Subject> page = searchSubjects.search(criteria);
        return PageResponse.of(page, subjectMapper::toResponse);
    }

    @GetMapping("/{id}")
    public SubjectResponseDTO get(@PathVariable Long id) {
        return subjectMapper.toResponse(getSubjectById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public SubjectResponseDTO update(@PathVariable Long id, @RequestBody SubjectRequestDTO request) {
        return subjectMapper.toResponse(updateSubject.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteSubject.delete(id);
    }
}
