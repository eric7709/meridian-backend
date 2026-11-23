package com.app.meridian.feePayment.dto;

import java.time.LocalDate;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record FeePaymentRequestDTO(Double amount, LocalDate paidOn, String reference, Long studentId, Long termId) {
}
