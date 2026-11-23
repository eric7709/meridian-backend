package com.app.meridian.student;

import com.app.meridian.common.PageResponse;
import com.app.meridian.student.dto.StudentRequestDTO;
import com.app.meridian.student.dto.StudentResponseDTO;
import com.app.meridian.student.dto.StudentSearchCriteria;
import com.app.meridian.student.services.CreateStudent;
import com.app.meridian.student.services.DeleteStudent;
import com.app.meridian.student.services.GetStudentById;
import com.app.meridian.student.services.SearchStudents;
import com.app.meridian.student.services.UpdateStudent;
import com.app.meridian.student.utils.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/students")
public class StudentController {

    private final CreateStudent createStudent;
    private final UpdateStudent updateStudent;
    private final DeleteStudent deleteStudent;
    private final GetStudentById getStudentById;
    private final SearchStudents searchStudents;
    private final StudentMapper studentMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public StudentResponseDTO create(@RequestBody StudentRequestDTO request) {
        return studentMapper.toResponse(createStudent.create(request));
    }

    @GetMapping
    public PageResponse<StudentResponseDTO> search(StudentSearchCriteria criteria) {
        Page<Student> page = searchStudents.search(criteria);
        return PageResponse.of(page, studentMapper::toResponse);
    }

    @GetMapping("/{id}")
    public StudentResponseDTO get(@PathVariable Long id) {
        return studentMapper.toResponse(getStudentById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public StudentResponseDTO update(@PathVariable Long id, @RequestBody StudentRequestDTO request) {
        return studentMapper.toResponse(updateStudent.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteStudent.delete(id);
    }
}
