package com.app.meridian.feePayment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.feePayment.dto.FeePaymentRequestDTO;
import com.app.meridian.feePayment.dto.FeePaymentResponseDTO;
import com.app.meridian.feePayment.dto.FeeSummaryDTO;
import com.app.meridian.feePayment.services.GetFeeSummary;
import com.app.meridian.feePayment.services.GetStudentFees;
import com.app.meridian.feePayment.services.RecordFeePayment;
import com.app.meridian.common.PageResponse;
import com.app.meridian.feePayment.dto.FeePaymentSearchCriteria;
import com.app.meridian.feePayment.services.SearchFeePayments;
import com.app.meridian.feePayment.utils.FeePaymentMapper;
import com.app.meridian.schoolSettings.services.ModuleGate;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

/** Optional module: every endpoint answers 404 while fees are switched off in settings. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/fees")
public class FeeController {

    private final RecordFeePayment recordFeePayment;
    private final GetFeeSummary getFeeSummary;
    private final GetStudentFees getStudentFees;
    private final SearchFeePayments searchFeePayments;
    private final ModuleGate moduleGate;
    private final FeePaymentMapper feePaymentMapper;

    /** Paged payment history, filterable by student, term, amount range and reference. */
    @GetMapping("/payments")
    public PageResponse<FeePaymentResponseDTO> search(FeePaymentSearchCriteria criteria) {
        moduleGate.requireFees();
        Page<FeePayment> page = searchFeePayments.search(criteria);
        return PageResponse.of(page, feePaymentMapper::toResponse);
    }

    @GetMapping("/summary")
    public FeeSummaryDTO summary(@RequestParam(required = false) Long termId) {
        return getFeeSummary.summary(termId);
    }

    @GetMapping("/students/{studentId}")
    public List<FeePaymentResponseDTO> forStudent(@PathVariable Long studentId) {
        return getStudentFees.forStudent(studentId).stream().map(feePaymentMapper::toResponse).toList();
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public FeePaymentResponseDTO record(@RequestBody FeePaymentRequestDTO request) {
        return feePaymentMapper.toResponse(recordFeePayment.record(request));
    }
}
