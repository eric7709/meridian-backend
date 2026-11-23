package com.app.meridian.gradeScale;

import com.app.meridian.common.PageResponse;
import com.app.meridian.gradeScale.dto.GradeScaleRequestDTO;
import com.app.meridian.gradeScale.dto.GradeScaleResponseDTO;
import com.app.meridian.gradeScale.dto.GradeScaleSearchCriteria;
import com.app.meridian.gradeScale.services.CreateGradeScale;
import com.app.meridian.gradeScale.services.DeleteGradeScale;
import com.app.meridian.gradeScale.services.GetGradeScaleById;
import com.app.meridian.gradeScale.services.SearchGradeScales;
import com.app.meridian.gradeScale.services.UpdateGradeScale;
import com.app.meridian.gradeScale.utils.GradeScaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/grading-scale")
public class GradeScaleController {

    private final CreateGradeScale createGradeScale;
    private final UpdateGradeScale updateGradeScale;
    private final DeleteGradeScale deleteGradeScale;
    private final GetGradeScaleById getGradeScaleById;
    private final SearchGradeScales searchGradeScales;
    private final GradeScaleMapper gradeScaleMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public GradeScaleResponseDTO create(@RequestBody GradeScaleRequestDTO request) {
        return gradeScaleMapper.toResponse(createGradeScale.create(request));
    }

    @GetMapping
    public PageResponse<GradeScaleResponseDTO> search(GradeScaleSearchCriteria criteria) {
        Page<GradeScale> page = searchGradeScales.search(criteria);
        return PageResponse.of(page, gradeScaleMapper::toResponse);
    }

    @GetMapping("/{id}")
    public GradeScaleResponseDTO get(@PathVariable Long id) {
        return gradeScaleMapper.toResponse(getGradeScaleById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public GradeScaleResponseDTO update(@PathVariable Long id, @RequestBody GradeScaleRequestDTO request) {
        return gradeScaleMapper.toResponse(updateGradeScale.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteGradeScale.delete(id);
    }
}
