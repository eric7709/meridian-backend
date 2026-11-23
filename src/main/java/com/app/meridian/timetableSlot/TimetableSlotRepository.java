package com.app.meridian.timetableSlot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.timetableSlot.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long>, JpaSpecificationExecutor<TimetableSlot> {

    List<TimetableSlot> findByTermIdOrderByStartTimeAsc(Long termId);
    List<TimetableSlot> findByTermIdAndAssignmentClassRoomId(Long termId, Long classId);

}
