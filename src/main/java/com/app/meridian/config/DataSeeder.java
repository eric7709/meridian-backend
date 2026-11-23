package com.app.meridian.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.classroom.ClassRoomRepository;
import com.app.meridian.department.Department;
import com.app.meridian.department.DepartmentRepository;
import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.subject.Subject;
import com.app.meridian.subject.SubjectRepository;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.classroom.enums.Level;
import com.app.meridian.subject.enums.SubjectLevel;
import com.app.meridian.term.enums.TermName;
import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.schoolSettings.SchoolSettingsRepository;
import com.app.meridian.announcement.Announcement;
import com.app.meridian.announcement.AnnouncementRepository;
import com.app.meridian.announcement.enums.AnnouncementAudience;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.StaffRepository;
import com.app.meridian.student.Student;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;
import com.app.meridian.staff.enums.EmploymentType;
import com.app.meridian.student.enums.Gender;
import com.app.meridian.user.enums.Role;
import com.app.meridian.staff.enums.StaffRole;
import com.app.meridian.student.enums.StudentStatus;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.app.meridian.timetableSlot.enums.DayOfWeek;

/**
 * Seeds a usable school on first boot (dev profile only): grading scale, one session with three
 * terms, classes, subjects, an admin, a teacher with two assignments, and three students.
 *
 * Every seeded account uses the password "Password@123".
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "meridian.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final String PASSWORD = "Password@123";

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicSessionRepository sessionRepository;
    private final TermRepository termRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final GradeScaleRepository gradeScaleRepository;
    private final AnnouncementRepository announcementRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed skipped: data already present");
            return;
        }
        seedGradeScale();
        Department sciences = department("Sciences");
        department("Languages");
        department("Commercial");
        AcademicSession session = session();
        Term first = term(session, TermName.FIRST, true, true);
        term(session, TermName.SECOND, false, false);
        term(session, TermName.THIRD, false, false);

        Subject maths = subject("Mathematics", "MTH101", SubjectLevel.ALL, sciences);
        Subject english = subject("English Language", "ENG101", SubjectLevel.ALL, null);
        Subject physics = subject("Physics", "PHY101", SubjectLevel.SS, sciences);
        subject("Economics", "ECO101", SubjectLevel.SS, null);

        ClassRoom ss2a = classRoom("SS2 A", Level.SS2, "A", session);
        ClassRoom jss1a = classRoom("JSS1 A", Level.JSS1, "A", session);

        User adminUser = user("ADM/001", "admin@meridian.test", "Adaeze Obi", Role.ADMIN);
        User teacherUser = user("TCH/001", "teacher@meridian.test", "Chinedu Bello", Role.TEACHER);
        Staff admin = staff(adminUser, "STF/001", StaffRole.ADMIN, sciences);
        Staff teacher = staff(teacherUser, "STF/002", StaffRole.TEACHER, sciences);
        ss2a.setFormTeacher(teacher);
        classRoomRepository.save(ss2a);

        TeachingAssignment mathsAssignment = assignment(teacher, maths, ss2a, session);
        TeachingAssignment physicsAssignment = assignment(teacher, physics, ss2a, session);

        timetableSlot(mathsAssignment, first, com.app.meridian.timetableSlot.enums.DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(9, 0), "Hall A");
        timetableSlot(physicsAssignment, first, com.app.meridian.timetableSlot.enums.DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Lab 1");

        student("STU/2026/001", "student1@meridian.test", "Amaka Nwosu", Gender.FEMALE, ss2a, session);
        student("STU/2026/002", "student2@meridian.test", "Tunde Salami", Gender.MALE, ss2a, session);
        student("STU/2026/003", "student3@meridian.test", "Grace Eze", Gender.FEMALE, jss1a, session);

        announcement(adminUser, "Welcome to the new session",
                "First term results are now open for teacher submission.", AnnouncementAudience.ALL);
        announcement(adminUser, "Staff briefing",
                "All teachers should submit their result sheets before the end of the week.",
                AnnouncementAudience.TEACHER);

        SchoolSettings settings = new SchoolSettings();
        settings.setName("Meridian Demo College");
        settings.setEmail("hello@meridian.test");
        settings.setPhone("+234 800 000 0000");
        settings.setWebsite("https://meridian.test");
        settings.setAddress("12 Learning Way, Lagos");
        settings.setDepartmentsEnabled(true);
        settings.setFeesEnabled(true);
        settings.setCurrentTerm(first);
        schoolSettingsRepository.save(settings);

        log.info("""

                ================ Meridian seed ================
                 admin    ADM/001 / admin@meridian.test
                 teacher  TCH/001 / teacher@meridian.test
                 student  STU/2026/001 / student1@meridian.test
                 password Password@123   (all accounts)
                ==================================================
                """);
    }

    private void seedGradeScale() {
        Object[][] rows = {
                {"A1", 75.0, 100.0, "Excellent"},
                {"B2", 70.0, 74.99, "Very good"},
                {"B3", 65.0, 69.99, "Good"},
                {"C4", 60.0, 64.99, "Credit"},
                {"C5", 55.0, 59.99, "Credit"},
                {"C6", 50.0, 54.99, "Credit"},
                {"D7", 45.0, 49.99, "Pass"},
                {"E8", 40.0, 44.99, "Pass"},
                {"F9", 0.0, 39.99, "Fail"},
        };
        for (Object[] row : rows) {
            GradeScale scale = new GradeScale();
            scale.setGrade((String) row[0]);
            scale.setMinScore((Double) row[1]);
            scale.setMaxScore((Double) row[2]);
            scale.setRemark((String) row[3]);
            gradeScaleRepository.save(scale);
        }
    }

    private Department department(String name) {
        Department department = new Department();
        department.setName(name);
        return departmentRepository.save(department);
    }

    private AcademicSession session() {
        AcademicSession session = new AcademicSession();
        session.setLabel("2026/2027");
        session.setStartDate(LocalDate.of(2026, 9, 14));
        session.setEndDate(LocalDate.of(2027, 7, 16));
        session.setIsCurrent(true);
        return sessionRepository.save(session);
    }

    private Term term(AcademicSession session, TermName name, boolean active, boolean studentView) {
        Term term = new Term();
        term.setSession(session);
        term.setName(name);
        term.setIsActive(active);
        term.setAllowTeacherSubmission(true);
        term.setAllowStudentView(studentView);
        term.setIsLocked(false);
        return termRepository.save(term);
    }

    private Subject subject(String name, String code, SubjectLevel level, Department department) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setCode(code);
        subject.setLevel(level);
        subject.setDepartment(department);
        return subjectRepository.save(subject);
    }

    private ClassRoom classRoom(String name, Level level, String arm, AcademicSession session) {
        ClassRoom classRoom = new ClassRoom();
        classRoom.setName(name);
        classRoom.setLevel(level);
        classRoom.setArm(arm);
        classRoom.setSession(session);
        return classRoomRepository.save(classRoom);
    }

    private User user(String identifier, String email, String fullName, Role role) {
        User user = new User();
        user.setIdentifier(identifier);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        return userRepository.save(user);
    }

    private Staff staff(User user, String staffNo, StaffRole role, Department department) {
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setStaffNo(staffNo);
        staff.setStaffRole(role);
        staff.setEmploymentType(EmploymentType.FULL_TIME);
        staff.setDepartment(department);
        return staffRepository.save(staff);
    }

    private TeachingAssignment assignment(Staff teacher, Subject subject, ClassRoom classRoom,
            AcademicSession session) {
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setTeacher(teacher);
        assignment.setSubject(subject);
        assignment.setClassRoom(classRoom);
        assignment.setSession(session);
        return assignmentRepository.save(assignment);
    }

    private TimetableSlot timetableSlot(TeachingAssignment assignment, Term term,
            com.app.meridian.timetableSlot.enums.DayOfWeek day, LocalTime start, LocalTime end, String room) {
        TimetableSlot slot = new TimetableSlot();
        slot.setAssignment(assignment);
        slot.setTerm(term);
        slot.setDayOfWeek(day);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setRoom(room);
        return timetableSlotRepository.save(slot);
    }

    private void student(String studentNo, String email, String fullName, Gender gender,
            ClassRoom classRoom, AcademicSession session) {
        User user = user(studentNo, email, fullName, Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        student.setStudentNo(studentNo);
        student.setGender(gender);
        student.setStatus(StudentStatus.ACTIVE);
        student.setCurrentClass(classRoom);
        studentRepository.save(student);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassRoom(classRoom);
        enrollment.setSession(session);
        enrollment.setEnrolledOn(session.getStartDate());
        enrollmentRepository.save(enrollment);
    }

    private void announcement(User author, String title, String content, AnnouncementAudience audience) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setAudience(audience);
        announcement.setCreatedBy(author);
        announcementRepository.save(announcement);
    }
}
