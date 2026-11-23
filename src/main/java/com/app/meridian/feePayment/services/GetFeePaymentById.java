package com.app.meridian.feePayment.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.feePayment.FeePayment;
import com.app.meridian.feePayment.FeePaymentRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetFeePaymentById {

    private final FeePaymentRepository feePaymentRepository;

    @Transactional(readOnly = true)
    public FeePayment get(Long id) {
        ValidationUtils.requireNonNull(id, "FeePayment ID");
        return feePaymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FeePayment not found"));
    }
}
