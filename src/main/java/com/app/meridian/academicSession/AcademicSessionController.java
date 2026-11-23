package com.app.meridian.academicSession;

import com.app.meridian.academicSession.dto.AcademicSessionRequestDTO;
import com.app.meridian.academicSession.dto.AcademicSessionResponseDTO;
import com.app.meridian.academicSession.dto.AcademicSessionSearchCriteria;
import com.app.meridian.academicSession.services.CreateAcademicSession;
import com.app.meridian.academicSession.services.DeleteAcademicSession;
import com.app.meridian.academicSession.services.GetAcademicSessionById;
import com.app.meridian.academicSession.services.SearchSessions;
import com.app.meridian.academicSession.services.UpdateAcademicSession;
import com.app.meridian.academicSession.utils.AcademicSessionMapper;
import com.app.meridian.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/sessions")
public class AcademicSessionController {

    private final CreateAcademicSession createAcademicSession;
    private final UpdateAcademicSession updateAcademicSession;
    private final DeleteAcademicSession deleteAcademicSession;
    private final GetAcademicSessionById getAcademicSessionById;
    private final SearchSessions searchSessions;
    private final AcademicSessionMapper academicSessionMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public AcademicSessionResponseDTO create(@RequestBody AcademicSessionRequestDTO request) {
        return academicSessionMapper.toResponse(createAcademicSession.create(request));
    }

    @GetMapping
    public PageResponse<AcademicSessionResponseDTO> search(AcademicSessionSearchCriteria criteria) {
        Page<AcademicSession> page = searchSessions.search(criteria);
        return PageResponse.of(page, academicSessionMapper::toResponse);
    }

    @GetMapping("/{id}")
    public AcademicSessionResponseDTO get(@PathVariable Long id) {
        return academicSessionMapper.toResponse(getAcademicSessionById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public AcademicSessionResponseDTO update(@PathVariable Long id, @RequestBody AcademicSessionRequestDTO request) {
        return academicSessionMapper.toResponse(updateAcademicSession.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteAcademicSession.delete(id);
    }
}
