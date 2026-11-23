package com.app.meridian.department;

import com.app.meridian.common.PageResponse;
import com.app.meridian.department.dto.DepartmentRequestDTO;
import com.app.meridian.department.dto.DepartmentResponseDTO;
import com.app.meridian.department.dto.DepartmentSearchCriteria;
import com.app.meridian.department.services.CreateDepartment;
import com.app.meridian.department.services.DeleteDepartment;
import com.app.meridian.department.services.GetDepartmentById;
import com.app.meridian.department.services.SearchDepartments;
import com.app.meridian.department.services.UpdateDepartment;
import com.app.meridian.department.utils.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/departments")
public class DepartmentController {

    private final CreateDepartment createDepartment;
    private final UpdateDepartment updateDepartment;
    private final DeleteDepartment deleteDepartment;
    private final GetDepartmentById getDepartmentById;
    private final SearchDepartments searchDepartments;
    private final DepartmentMapper departmentMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public DepartmentResponseDTO create(@RequestBody DepartmentRequestDTO request) {
        return departmentMapper.toResponse(createDepartment.create(request));
    }

    @GetMapping
    public PageResponse<DepartmentResponseDTO> search(DepartmentSearchCriteria criteria) {
        Page<Department> page = searchDepartments.search(criteria);
        return PageResponse.of(page, departmentMapper::toResponse);
    }

    @GetMapping("/{id}")
    public DepartmentResponseDTO get(@PathVariable Long id) {
        return departmentMapper.toResponse(getDepartmentById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public DepartmentResponseDTO update(@PathVariable Long id, @RequestBody DepartmentRequestDTO request) {
        return departmentMapper.toResponse(updateDepartment.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteDepartment.delete(id);
    }
}
