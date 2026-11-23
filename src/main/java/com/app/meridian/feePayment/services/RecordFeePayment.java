package com.app.meridian.feePayment.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.feePayment.FeePayment;
import com.app.meridian.feePayment.FeePaymentEvents;
import com.app.meridian.feePayment.FeePaymentRepository;
import com.app.meridian.feePayment.dto.FeePaymentRequestDTO;
import com.app.meridian.feePayment.utils.FeePaymentMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.student.Student;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.schoolSettings.services.ModuleGate;
import com.app.meridian.term.Term;

/** Records a payment; disabled (404) when the fees module is off. */
@Service
@RequiredArgsConstructor
public class RecordFeePayment {

    private final ModuleGate moduleGate;
    private final EntityResolver entityResolver;
    private final FeePaymentRepository feePaymentRepository;
    private final FeePaymentEvents feePaymentEvents;
    private final FeePaymentMapper feePaymentMapper;
    private final AuditTrail auditTrail;

    @Transactional
    public FeePayment record(FeePaymentRequestDTO request) {
        moduleGate.requireFees();
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.studentId(), "Student");
        ValidationUtils.requireNonNull(request.termId(), "Term");
        ValidationUtils.requireGreaterThanZero(request.amount(), "Amount");
        ValidationUtils.requireNonBlank(request.reference(), "Payment reference");
        Student student = entityResolver.resolveStudent(request.studentId());
        if (feePaymentRepository.existsByReferenceIgnoreCase(request.reference().trim())) {
            throw new ConflictException("A payment with this reference has already been recorded");
        }
        FeePayment payment = new FeePayment();
        payment.setStudent(student);
        payment.setTerm(entityResolver.resolveTerm(request.termId()));
        payment.setAmount(request.amount());
        payment.setPaidOn(request.paidOn() == null ? LocalDate.now() : request.paidOn());
        payment.setReference(request.reference().trim());
        FeePayment saved = feePaymentRepository.save(payment);
        feePaymentEvents.broadcastCreated(feePaymentMapper.toResponse(saved));
        auditTrail.record("FEE_PAYMENT_RECORDED", "FeePayment", saved.getId(),
                "Payment of " + saved.getAmount() + " for " + student.getStudentNo());
        return saved;
    }
}
