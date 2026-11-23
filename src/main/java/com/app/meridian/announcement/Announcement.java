package com.app.meridian.announcement;

import java.time.LocalDateTime;
import com.app.meridian.announcement.enums.AnnouncementAudience;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false)
    private AnnouncementAudience audience;

    @Column(name = "event_at", nullable = true)
    private LocalDateTime eventAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "createdBy_id", nullable = true, foreignKey = @ForeignKey(name = "fk_announcements_createdBy"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User createdBy;

}
