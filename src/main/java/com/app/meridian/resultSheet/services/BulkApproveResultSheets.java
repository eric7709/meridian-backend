package com.app.meridian.resultSheet.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.resultSheet.ResultSheet;

/** "Approve all" on the pending approvals screen: each sheet is approved independently. */
@Service
@RequiredArgsConstructor
public class BulkApproveResultSheets {

    private final ApproveResultSheet approveResultSheet;
    private final AuditTrail auditTrail;

    @Transactional
    public List<Long> approve(List<Long> sheetIds) {
        ValidationUtils.requireNotEmpty(sheetIds, "Result sheet ids");
        List<Long> approved = new ArrayList<>();
        for (Long id : sheetIds) {
            approveResultSheet.approve(id);
            approved.add(id);
        }
        auditTrail.record("RESULT_SHEETS_BULK_APPROVED", "ResultSheet", null,
                "Bulk approved " + approved.size() + " sheet(s)");
        return approved;
    }
}
