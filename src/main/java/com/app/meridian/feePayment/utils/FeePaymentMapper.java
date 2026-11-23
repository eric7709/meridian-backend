package com.app.meridian.feePayment.utils;

import java.util.List;
import com.app.meridian.feePayment.FeePayment;
import com.app.meridian.feePayment.dto.FeePaymentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeePaymentMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "studentName", source = "student.user.fullName")
    @Mapping(target = "studentNo", source = "student.studentNo")
    FeePaymentResponseDTO toResponse(FeePayment entity);

    List<FeePaymentResponseDTO> mapAllToResponse(List<FeePayment> entities);
}
