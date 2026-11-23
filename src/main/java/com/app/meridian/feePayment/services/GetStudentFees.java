package com.app.meridian.feePayment.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.feePayment.FeePayment;
import com.app.meridian.feePayment.FeePaymentRepository;
import com.app.meridian.student.services.GetCurrentStudent;

import lombok.RequiredArgsConstructor;
import com.app.meridian.schoolSettings.services.ModuleGate;

@Service
@RequiredArgsConstructor
public class GetStudentFees {

    private final ModuleGate moduleGate;
    private final FeePaymentRepository feePaymentRepository;
    private final GetCurrentStudent getCurrentStudent;

    @Transactional(readOnly = true)
    public List<FeePayment> forStudent(Long studentId) {
        moduleGate.requireFees();
        Long target = studentId == null ? getCurrentStudent.get().getId() : studentId;
        return feePaymentRepository.findByStudentId(target);
    }
}
