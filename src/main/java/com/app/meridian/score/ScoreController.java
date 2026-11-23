package com.app.meridian.score;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.common.PageResponse;
import com.app.meridian.score.dto.ScoreResponseDTO;
import com.app.meridian.score.dto.ScoreSearchCriteria;
import com.app.meridian.score.services.SearchScores;
import com.app.meridian.score.utils.ScoreMapper;

import lombok.RequiredArgsConstructor;

/** Read-only score search (entry and upload go through /result-sheets/{id}/scores). */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/scores")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
public class ScoreController {

    private final SearchScores searchScores;
    private final ScoreMapper scoreMapper;

    @GetMapping
    public PageResponse<ScoreResponseDTO> search(ScoreSearchCriteria criteria) {
        Page<Score> page = searchScores.search(criteria);
        return PageResponse.of(page, scoreMapper::toResponse);
    }
}
