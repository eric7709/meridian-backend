package com.app.meridian.classroom;

import com.app.meridian.classroom.dto.ClassRoomRequestDTO;
import com.app.meridian.classroom.dto.ClassRoomResponseDTO;
import com.app.meridian.classroom.dto.ClassRoomSearchCriteria;
import com.app.meridian.classroom.services.CreateClassRoom;
import com.app.meridian.classroom.services.DeleteClassRoom;
import com.app.meridian.classroom.services.GetClassRoomById;
import com.app.meridian.classroom.services.SearchClasses;
import com.app.meridian.classroom.services.UpdateClassRoom;
import com.app.meridian.classroom.utils.ClassRoomMapper;
import com.app.meridian.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/classes")
public class ClassRoomController {

    private final CreateClassRoom createClassRoom;
    private final UpdateClassRoom updateClassRoom;
    private final DeleteClassRoom deleteClassRoom;
    private final GetClassRoomById getClassRoomById;
    private final SearchClasses searchClasses;
    private final ClassRoomMapper classRoomMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public ClassRoomResponseDTO create(@RequestBody ClassRoomRequestDTO request) {
        return classRoomMapper.toResponse(createClassRoom.create(request));
    }

    @GetMapping
    public PageResponse<ClassRoomResponseDTO> search(ClassRoomSearchCriteria criteria) {
        Page<ClassRoom> page = searchClasses.search(criteria);
        return PageResponse.of(page, classRoomMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ClassRoomResponseDTO get(@PathVariable Long id) {
        return classRoomMapper.toResponse(getClassRoomById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public ClassRoomResponseDTO update(@PathVariable Long id, @RequestBody ClassRoomRequestDTO request) {
        return classRoomMapper.toResponse(updateClassRoom.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteClassRoom.delete(id);
    }
}
