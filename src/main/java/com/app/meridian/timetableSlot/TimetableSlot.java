package com.app.meridian.timetableSlot;

import java.time.LocalTime;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.term.Term;
import com.app.meridian.timetableSlot.enums.DayOfWeek;
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
@Table(name = "timetable_slots")
public class TimetableSlot extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "room", nullable = true)
    private String room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_timetable_slots_assignment"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private TeachingAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id", nullable = false, foreignKey = @ForeignKey(name = "fk_timetable_slots_term"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Term term;

}
