package com.app.meridian.staff;

import com.app.meridian.common.PageResponse;
import com.app.meridian.staff.dto.StaffRequestDTO;
import com.app.meridian.staff.dto.StaffResponseDTO;
import com.app.meridian.staff.dto.StaffSearchCriteria;
import com.app.meridian.staff.services.CreateStaff;
import com.app.meridian.staff.services.DeleteStaff;
import com.app.meridian.staff.services.GetStaffById;
import com.app.meridian.staff.services.SearchStaff;
import com.app.meridian.staff.services.UpdateStaff;
import com.app.meridian.staff.utils.StaffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/staff")
public class StaffController {

    private final CreateStaff createStaff;
    private final UpdateStaff updateStaff;
    private final DeleteStaff deleteStaff;
    private final GetStaffById getStaffById;
    private final SearchStaff searchStaff;
    private final StaffMapper staffMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public StaffResponseDTO create(@RequestBody StaffRequestDTO request) {
        return staffMapper.toResponse(createStaff.create(request));
    }

    @GetMapping
    public PageResponse<StaffResponseDTO> search(StaffSearchCriteria criteria) {
        Page<Staff> page = searchStaff.search(criteria);
        return PageResponse.of(page, staffMapper::toResponse);
    }

    @GetMapping("/{id}")
    public StaffResponseDTO get(@PathVariable Long id) {
        return staffMapper.toResponse(getStaffById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public StaffResponseDTO update(@PathVariable Long id, @RequestBody StaffRequestDTO request) {
        return staffMapper.toResponse(updateStaff.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteStaff.delete(id);
    }
}
