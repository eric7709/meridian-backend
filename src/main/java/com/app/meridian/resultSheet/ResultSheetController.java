package com.app.meridian.resultSheet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.meridian.common.PageResponse;
import com.app.meridian.resultSheet.dto.RejectSheetRequestDTO;
import com.app.meridian.resultSheet.dto.ResultSheetDraftRequestDTO;
import com.app.meridian.resultSheet.dto.ResultSheetResponseDTO;
import com.app.meridian.resultSheet.dto.ResultSheetSearchCriteria;
import com.app.meridian.score.dto.ScoreEntryRequestDTO;
import com.app.meridian.score.dto.ScoreResponseDTO;
import com.app.meridian.resultSheet.dto.ScoreRosterDTO;
import com.app.meridian.resultSheet.dto.UploadScoresResultDTO;
import com.app.meridian.resultSheet.services.ApproveResultSheet;
import com.app.meridian.resultSheet.services.BulkApproveResultSheets;
import com.app.meridian.resultSheet.services.CreateResultSheet;
import com.app.meridian.resultSheet.services.ExportResultSheetCsv;
import com.app.meridian.resultSheet.services.GetResultSheetById;
import com.app.meridian.resultSheet.services.GetResultSheetScores;
import com.app.meridian.resultSheet.services.ListResultSheets;
import com.app.meridian.resultSheet.services.RejectResultSheet;
import com.app.meridian.score.services.SaveScore;
import com.app.meridian.resultSheet.services.SubmitResultSheet;
import com.app.meridian.score.services.UploadScores;
import com.app.meridian.resultSheet.utils.ResultSheetMapper;
import com.app.meridian.score.utils.ScoreMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/result-sheets")
public class ResultSheetController {

    private final CreateResultSheet createResultSheet;
    private final SubmitResultSheet submitResultSheet;
    private final ApproveResultSheet approveResultSheet;
    private final RejectResultSheet rejectResultSheet;
    private final BulkApproveResultSheets bulkApproveResultSheets;
    private final GetResultSheetById getResultSheetById;
    private final GetResultSheetScores getResultSheetScores;
    private final ListResultSheets listResultSheets;
    private final SaveScore saveScore;
    private final UploadScores uploadScores;
    private final ExportResultSheetCsv exportResultSheetCsv;
    private final ResultSheetMapper resultSheetMapper;
    private final ScoreMapper scoreMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public PageResponse<ResultSheetResponseDTO> search(ResultSheetSearchCriteria criteria) {
        Page<ResultSheet> page = listResultSheets.search(criteria);
        return PageResponse.of(page, resultSheetMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ResultSheetResponseDTO get(@PathVariable Long id) {
        return resultSheetMapper.toResponse(getResultSheetById.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public ResultSheetResponseDTO create(@RequestBody ResultSheetDraftRequestDTO request) {
        return resultSheetMapper.toResponse(createResultSheet.create(request));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public ResultSheetResponseDTO submit(@PathVariable Long id) {
        return resultSheetMapper.toResponse(submitResultSheet.submit(id));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public ResultSheetResponseDTO approve(@PathVariable Long id) {
        return resultSheetMapper.toResponse(approveResultSheet.approve(id));
    }

    @PostMapping("/bulk-approve")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public List<Long> bulkApprove(@RequestBody List<Long> sheetIds) {
        return bulkApproveResultSheets.approve(sheetIds);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public ResultSheetResponseDTO reject(@PathVariable Long id, @RequestBody RejectSheetRequestDTO request) {
        return resultSheetMapper.toResponse(rejectResultSheet.reject(id, request));
    }

    @GetMapping("/{id}/scores")
    public ScoreRosterDTO scores(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String marked) {
        return getResultSheetScores.roster(id, search, marked);
    }

    @PutMapping("/{id}/scores/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public ScoreResponseDTO saveScore(@PathVariable Long id, @PathVariable Long studentId,
            @RequestBody ScoreEntryRequestDTO request) {
        return scoreMapper.toResponse(saveScore.save(id, studentId, request));
    }

    @PostMapping(value = "/{id}/scores/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public UploadScoresResultDTO upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return uploadScores.upload(id, file);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        byte[] csv = exportResultSheetCsv.export(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"result-sheet-" + id + ".csv\"")
                .contentType(MediaType.valueOf("text/csv"))
                .body(csv);
    }
}
