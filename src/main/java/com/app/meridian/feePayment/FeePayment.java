package com.app.meridian.feePayment;

import java.time.LocalDate;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.student.Student;
import com.app.meridian.term.Term;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "fee_payments")
public class FeePayment extends BaseEntity {

    @NotNull
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull
    @Column(name = "paid_on", nullable = false)
    private LocalDate paidOn;

    @NotNull
    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fee_payments_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fee_payments_term"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Term term;

}
