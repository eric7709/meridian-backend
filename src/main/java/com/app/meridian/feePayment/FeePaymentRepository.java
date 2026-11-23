package com.app.meridian.feePayment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long>, JpaSpecificationExecutor<FeePayment> {

    boolean existsByReferenceIgnoreCase(String value);
    boolean existsByReferenceIgnoreCaseAndIdNot(String value, Long id);
    Optional<FeePayment> findByReferenceIgnoreCase(String value);
    List<FeePayment> findByStudentId(Long studentId);
    List<FeePayment> findByTermId(Long termId);
    List<FeePayment> findByStudentIdAndTermId(Long studentId, Long termId);

}
