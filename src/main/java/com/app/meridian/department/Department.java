package com.app.meridian.department;

import com.app.meridian.common.BaseEntity;
import com.app.meridian.staff.Staff;
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
@Table(name = "departments")
public class Department extends BaseEntity {

    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "headStaff_id", nullable = true, foreignKey = @ForeignKey(name = "fk_departments_headStaff"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Staff headStaff;

}
