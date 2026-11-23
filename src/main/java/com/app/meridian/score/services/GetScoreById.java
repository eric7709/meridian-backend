package com.app.meridian.score.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetScoreById {

    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public Score get(Long id) {
        ValidationUtils.requireNonNull(id, "Score ID");
        return scoreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Score not found"));
    }
}
