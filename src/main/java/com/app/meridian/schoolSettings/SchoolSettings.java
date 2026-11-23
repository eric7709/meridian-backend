package com.app.meridian.schoolSettings;

import com.app.meridian.common.BaseEntity;
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
@Table(name = "school_settings")
public class SchoolSettings extends BaseEntity {

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "website", nullable = true)
    private String website;

    @Column(name = "address", nullable = true, columnDefinition = "TEXT")
    private String address;

    @Column(name = "logo_url", nullable = true)
    private String logoUrl;

    @NotNull
    @Column(name = "departments_enabled", nullable = false)
    private Boolean departmentsEnabled;

    @NotNull
    @Column(name = "fees_enabled", nullable = false)
    private Boolean feesEnabled;

    @Column(name = "notification_prefs", nullable = true, columnDefinition = "TEXT")
    private String notificationPrefs;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currentTerm_id", nullable = true, foreignKey = @ForeignKey(name = "fk_school_settings_currentTerm"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Term currentTerm;

}
