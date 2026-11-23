package com.app.meridian.feePayment.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.feePayment.FeePayment;
import com.app.meridian.feePayment.FeePaymentRepository;
import com.app.meridian.feePayment.dto.FeeSummaryDTO;
import com.app.meridian.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;
import com.app.meridian.schoolSettings.services.ModuleGate;

/** "Fees collected" card: totals for the term, or for everything when no term is given. */
@Service
@RequiredArgsConstructor
public class GetFeeSummary {

    private final ModuleGate moduleGate;
    private final FeePaymentRepository feePaymentRepository;

    @Transactional(readOnly = true)
    public FeeSummaryDTO summary(Long termId) {
        moduleGate.requireFees();
        List<FeePayment> payments = termId == null
                ? feePaymentRepository.findAll()
                : feePaymentRepository.findByTermId(termId);
        if (termId != null && payments.isEmpty() && !feePaymentRepository.findByTermId(termId).isEmpty()) {
            throw new BadRequestException("Could not read fee payments for that term");
        }
        double collected = Math.round(payments.stream()
                .mapToDouble(payment -> payment.getAmount() == null ? 0 : payment.getAmount())
                .sum() * 100.0) / 100.0;
        long students = payments.stream()
                .map(payment -> payment.getStudent().getId())
                .distinct()
                .count();
        return new FeeSummaryDTO(termId, collected, payments.size(), students);
    }
}
