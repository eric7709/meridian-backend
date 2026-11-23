package com.app.meridian.academicSession;

import java.time.LocalDate;
import com.app.meridian.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "academic_sessions")
public class AcademicSession extends BaseEntity {

    @NotNull
    @Column(name = "label", nullable = false, unique = true)
    private String label;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

}
