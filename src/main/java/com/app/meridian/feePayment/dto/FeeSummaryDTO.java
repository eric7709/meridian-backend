package com.app.meridian.feePayment.dto;

public record FeeSummaryDTO(Long termId, Double collected, long payments, long studentsPaid) {
}
