package com.app.meridian.feePayment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeePaymentResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double amount;
    private LocalDate paidOn;
    private String reference;
    private Long studentId;
    private Long termId;
    private String studentName;
    private String studentNo;
}
