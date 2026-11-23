package com.app.meridian.portal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.common.PageResponse;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.teachingAssignment.dto.MyStudentRowDTO;
import com.app.meridian.teachingAssignment.dto.MySubjectDTO;
import com.app.meridian.resultSheet.dto.ResultSheetResponseDTO;
import com.app.meridian.resultSheet.dto.ResultSheetSearchCriteria;
import com.app.meridian.teachingAssignment.services.GetMyStudents;
import com.app.meridian.teachingAssignment.services.GetMySubjects;
import com.app.meridian.resultSheet.services.SearchResultSheets;
import com.app.meridian.resultSheet.utils.ResultSheetMapper;

import lombok.RequiredArgsConstructor;
import com.app.meridian.resultSheet.ResultSheet;

/**
 * Everything under /teachers/me is scoped to the signed-in teacher: their subjects, their students
 * and their own result-sheet history. The teacher id is taken from the token, never from the query.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/teachers/me")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
public class TeacherPortalController {

    private final GetMySubjects getMySubjects;
    private final GetMyStudents getMyStudents;
    private final SearchResultSheets searchResultSheets;
    private final GetCurrentStaff getCurrentStaff;
    private final ResultSheetMapper resultSheetMapper;

    @GetMapping("/subjects")
    public List<MySubjectDTO> subjects(@RequestParam(required = false) String session) {
        return getMySubjects.get(session);
    }

    @GetMapping("/students")
    public List<MyStudentRowDTO> students(@RequestParam(required = false) String search,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId) {
        return getMyStudents.get(search, classId, subjectId);
    }

    @GetMapping("/result-sheets")
    public PageResponse<ResultSheetResponseDTO> resultSheets(ResultSheetSearchCriteria criteria,
            @RequestParam(required = false) Long subjectId) {
        criteria.setTeacherId(getCurrentStaff.get().getId());
        Page<ResultSheet> page = searchResultSheets.search(criteria);
        return PageResponse.of(page, resultSheetMapper::toResponse);
    }
}
