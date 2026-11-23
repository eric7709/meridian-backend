package com.app.meridian.term.utils;

import java.util.List;
import com.app.meridian.term.Term;
import com.app.meridian.term.dto.TermResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TermMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "sessionLabel", source = "session.label")
    TermResponseDTO toResponse(Term entity);

    List<TermResponseDTO> mapAllToResponse(List<Term> entities);
}
