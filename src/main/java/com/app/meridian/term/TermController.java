package com.app.meridian.term;

import com.app.meridian.common.PageResponse;
import com.app.meridian.term.dto.TermRequestDTO;
import com.app.meridian.term.dto.TermResponseDTO;
import com.app.meridian.term.dto.TermSearchCriteria;
import com.app.meridian.term.services.CreateTerm;
import com.app.meridian.term.services.DeleteTerm;
import com.app.meridian.term.services.GetTermById;
import com.app.meridian.term.services.SearchTerms;
import com.app.meridian.term.services.UpdateTerm;
import com.app.meridian.term.utils.TermMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/terms")
public class TermController {

    private final CreateTerm createTerm;
    private final UpdateTerm updateTerm;
    private final DeleteTerm deleteTerm;
    private final GetTermById getTermById;
    private final SearchTerms searchTerms;
    private final TermMapper termMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public TermResponseDTO create(@RequestBody TermRequestDTO request) {
        return termMapper.toResponse(createTerm.create(request));
    }

    @GetMapping
    public PageResponse<TermResponseDTO> search(TermSearchCriteria criteria) {
        Page<Term> page = searchTerms.search(criteria);
        return PageResponse.of(page, termMapper::toResponse);
    }

    @GetMapping("/{id}")
    public TermResponseDTO get(@PathVariable Long id) {
        return termMapper.toResponse(getTermById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public TermResponseDTO update(@PathVariable Long id, @RequestBody TermRequestDTO request) {
        return termMapper.toResponse(updateTerm.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteTerm.delete(id);
    }
}
