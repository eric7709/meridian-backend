"""Single source of truth for the generated modules.

Every entity below produces, automatically:
  entity, repository (with uniqueness probes), <N>RequestDTO, <N>ResponseDTO,
  <N>SearchCriteria, <N>Mapper (MapStruct), <N>Events (STOMP broadcaster),
  Search<N>s + Get<N>ById services, and — when crud=True — Create/Update/Delete
  services plus a REST controller.

Flag legend
  crud=True      full generated CRUD + controller; False = scaffolding only, the
                 service/controller layer is hand-written because it carries rules
  req=True       NOT NULL
  uniq=True      unique probe generated (ignore-case for strings)
  search=True    included in the free-text `search` predicate
  filter=True    exposed as an equality filter on the search criteria
  range=True     exposed as min<Field> / max<Field> on the search criteria
  default        value applied on create when the caller omits it
  autocode       (field, prefix) -> PREFIX/YYYY/NNN when the caller omits it
  text=True      stored as TEXT (long free text)
  resp=False     never leaves the API (passwords)
"""


def f(name, type, **kw):
    d = dict(name=name, type=type, req=False, uniq=False, search=False,
             filter=False, range=False, default=None, text=False, resp=True,
             request=True)
    d.update(kw)
    return d


def r(name, target, col=None, null=True, ondelete="SET_NULL", display=None,
      one_to_one=False, filter=False, request_name=None, response_name=None):
    return dict(name=name, target=target, col=col or (name + "_id"), null=null,
                ondelete=ondelete, display=display, one_to_one=one_to_one,
                filter=filter, request_name=request_name,
                response_name=response_name)


ENTITIES = [
    # --------------------------------------------------------------- identity
    dict(
        name="User", domain="identity", table="users", path="users",
        plural="Users", crud=False, autocode=None,
        fields=[
            f("identifier", "String", req=True, uniq=True, search=True),
            f("email", "String", req=True, uniq=True, search=True),
            f("passwordHash", "String", req=True, resp=False, request=False,
              text=False),
            f("fullName", "String", req=True, search=True),
            f("role", "Role", req=True, filter=True),
            f("active", "Boolean", req=True, default="true", filter=True),
            f("lastLoginAt", "LocalDateTime"),
        ],
        relations=[], extras=[],
    ),
    # ------------------------------------------------------------------ people
    dict(
        name="Student", domain="identity", table="students", path="students",
        plural="Students", crud=True, autocode=("studentNo", "STU"),
        # the student's own name lives on the login record, not on the student row
        related_search=["user.fullName", "user.email"],
        fields=[
            f("studentNo", "String", uniq=True, search=True),
            f("gender", "Gender"),
            f("dob", "LocalDate"),
            f("phone", "String"),
            f("guardianName", "String", search=True),
            f("guardianPhone", "String"),
            f("status", "StudentStatus", req=True, default="ACTIVE", filter=True),
        ],
        relations=[
            r("user", "User", null=False, ondelete="CASCADE", one_to_one=True,
              display="fullName"),
            r("currentClass", "ClassRoom", null=True, ondelete="SET_NULL",
              display="name", filter=True),
        ],
        extras=[("userFullName", "user.fullName"), ("userEmail", "user.email")],
        repo_methods=[
            "Optional<Student> findByUserId(Long userId);",
        ],
    ),
    dict(
        name="Staff", domain="identity", table="staff", path="staff",
        plural="Staff", crud=True, autocode=("staffNo", "STF"),
        related_search=["user.fullName", "user.email"],
        fields=[
            f("staffNo", "String", uniq=True, search=True),
            f("staffRole", "StaffRole", req=True, filter=True),
            f("employmentType", "EmploymentType", req=True,
              default="FULL_TIME", filter=True),
            f("phone", "String"),
        ],
        relations=[
            r("user", "User", null=False, ondelete="CASCADE", one_to_one=True,
              display="fullName"),
            r("department", "Department", null=True, ondelete="SET_NULL",
              display="name", filter=True),
        ],
        extras=[("userFullName", "user.fullName"), ("userEmail", "user.email")],
        repo_methods=[
            "Optional<Staff> findByUserId(Long userId);",
            "List<Staff> findByStaffRole(StaffRole staffRole);",
        ],
    ),
    # ------------------------------------------------------- academic structure
    dict(
        name="Department", domain="academic", table="departments",
        path="departments", plural="Departments", crud=True,
        fields=[f("name", "String", req=True, uniq=True, search=True)],
        relations=[r("headStaff", "Staff", null=True, ondelete="SET_NULL",
                     display=None)],
        extras=[("headStaffName", "headStaff.user.fullName")],
        module_gate="departments",
    ),
    dict(
        name="AcademicSession", domain="academic", table="academic_sessions",
        path="sessions", plural="Sessions", crud=True,
        fields=[
            f("label", "String", req=True, uniq=True, search=True),
            f("startDate", "LocalDate", req=True, range=True),
            f("endDate", "LocalDate", req=True, range=True),
            f("isCurrent", "Boolean", req=True, default="false", filter=True),
        ],
        relations=[],
        repo_methods=[
            "Optional<AcademicSession> findByIsCurrentTrue();",
        ],
    ),
    dict(
        name="Term", domain="academic", table="terms", path="terms",
        plural="Terms", crud=True, cache_evict=["activeTerm"],
        fields=[
            f("name", "TermName", req=True, filter=True),
            f("isActive", "Boolean", req=True, default="false", filter=True),
            f("allowTeacherSubmission", "Boolean", req=True, default="true"),
            f("allowStudentView", "Boolean", req=True, default="false"),
            f("isLocked", "Boolean", req=True, default="false", filter=True),
        ],
        relations=[r("session", "AcademicSession", null=False,
                     ondelete="CASCADE", display="label")],
        unique_together=[("session", "name")],
        repo_methods=[
            "Optional<Term> findByIsActiveTrue();",
            "List<Term> findBySessionId(Long sessionId);",
        ],
    ),
    dict(
        name="ClassRoom", domain="academic", table="classrooms", path="classes",
        plural="Classes", crud=True,
        fields=[
            f("name", "String", req=True, uniq=True, search=True),
            f("level", "Level", req=True, filter=True),
            f("arm", "String", search=True),
        ],
        relations=[
            r("formTeacher", "Staff", null=True, ondelete="SET_NULL"),
            r("session", "AcademicSession", null=True, ondelete="SET_NULL",
              display="label"),
        ],
        extras=[("formTeacherName", "formTeacher.user.fullName")],
    ),
    dict(
        name="Subject", domain="academic", table="subjects", path="subjects",
        plural="Subjects", crud=True, autocode=("code", "SUB"),
        fields=[
            f("name", "String", req=True, search=True),
            # optional: junior secondary schools often have no subject codes, so one is
            # generated (SUB/2026/001) when the caller leaves it out
            f("code", "String", req=False, uniq=True, search=True),
            f("level", "SubjectLevel", req=True, default="ALL", filter=True),
        ],
        relations=[r("department", "Department", null=True,
                     ondelete="SET_NULL")],
    ),
    dict(
        name="Enrollment", domain="academic", table="enrollments",
        path="enrollments", plural="Enrollments", crud=False,
        fields=[f("enrolledOn", "LocalDate", req=True, default="today",
                  range=True)],
        relations=[
            r("student", "Student", null=False, ondelete="CASCADE"),
            r("classRoom", "ClassRoom", col="class_id", null=False,
              ondelete="CASCADE", display="name", request_name="classId",
              response_name="classId"),
            r("session", "AcademicSession", null=False, ondelete="CASCADE",
              display="label"),
        ],
        unique_together=[("student", "session")],
        extras=[("studentName", "student.user.fullName"),
                ("studentNo", "student.studentNo"),
                ("className", "classRoom.name")],
        repo_methods=[
            "List<Enrollment> findByStudentIdOrderByEnrolledOnDesc(Long studentId);",
            "List<Enrollment> findByClassRoomIdAndSessionId(Long classId, Long sessionId);",
            "Optional<Enrollment> findByStudentIdAndSessionId(Long studentId, Long sessionId);",
            "List<Enrollment> findBySessionId(Long sessionId);",
        ],
    ),
    dict(
        name="TeachingAssignment", domain="academic",
        table="teaching_assignments", path="assignments",
        plural="TeachingAssignments", crud=True,
        fields=[],
        relations=[
            r("teacher", "Staff", null=False, ondelete="CASCADE", filter=True),
            r("subject", "Subject", null=False, ondelete="CASCADE", filter=True),
            r("classRoom", "ClassRoom", col="class_id", null=False,
              ondelete="CASCADE", filter=True, request_name="classId",
              response_name="classId"),
            r("session", "AcademicSession", null=False, ondelete="CASCADE",
              filter=True),
        ],
        unique_together=[("teacher", "subject", "classRoom", "session")],
        extras=[("teacherName", "teacher.user.fullName"),
                ("subjectName", "subject.name"), ("subjectCode", "subject.code"),
                ("className", "classRoom.name")],
        repo_methods=[
            "List<TeachingAssignment> findByTeacherIdAndSessionId(Long teacherId, Long sessionId);",
            "List<TeachingAssignment> findByTeacherId(Long teacherId);",
            "List<TeachingAssignment> findBySessionId(Long sessionId);",
        ],
    ),
    dict(
        name="TimetableSlot", domain="academic", table="timetable_slots",
        path="timetable", plural="TimetableSlots", crud=False,
        fields=[
            f("dayOfWeek", "DayOfWeek", req=True, filter=True),
            f("startTime", "LocalTime", req=True),
            f("endTime", "LocalTime", req=True),
            f("room", "String", search=True, filter=True),
        ],
        relations=[
            r("assignment", "TeachingAssignment", null=False,
              ondelete="CASCADE", filter=True),
            r("term", "Term", null=False, ondelete="CASCADE", filter=True),
        ],
        extras=[("subjectName", "assignment.subject.name"),
                ("className", "assignment.classRoom.name"),
                ("teacherName", "assignment.teacher.user.fullName")],
        repo_methods=[
            "List<TimetableSlot> findByTermIdOrderByStartTimeAsc(Long termId);",
            "List<TimetableSlot> findByTermIdAndAssignmentClassRoomId(Long termId, Long classId);",
        ],
    ),
    # --------------------------------------------------------- results engine
    dict(
        name="ResultSheet", domain="results", table="result_sheets",
        path="result-sheets", plural="ResultSheets", crud=False,
        fields=[
            f("code", "String", req=True, uniq=True, search=True),
            f("status", "ResultSheetStatus", req=True, default="DRAFT",
              filter=True),
            f("submittedAt", "LocalDateTime", range=True),
            f("reviewedAt", "LocalDateTime"),
            f("rejectionReason", "String", text=True),
        ],
        relations=[
            r("assignment", "TeachingAssignment", null=False,
              ondelete="CASCADE"),
            r("term", "Term", null=False, ondelete="CASCADE"),
            r("submittedBy", "Staff", null=True, ondelete="SET_NULL"),
            r("reviewedBy", "Staff", null=True, ondelete="SET_NULL"),
        ],
        unique_together=[("assignment", "term")],
        filters=[("classId", "Long", "assignment.classRoom.id"),
                 ("sessionId", "Long", "term.session.id"),
                 ("teacherId", "Long", "assignment.teacher.id"),
                 ("termId", "Long", "term.id")],
        extras=[("subjectName", "assignment.subject.name"),
                ("className", "assignment.classRoom.name"),
                ("teacherName", "assignment.teacher.user.fullName"),
                ("termName", "term.name"),
                ("sessionLabel", "term.session.label"),
                ("submittedByName", "submittedBy.user.fullName"),
                ("reviewedByName", "reviewedBy.user.fullName")],
        repo_methods=[
            "Optional<ResultSheet> findByAssignmentIdAndTermId(Long assignmentId, Long termId);",
            "List<ResultSheet> findByAssignmentTeacherId(Long teacherId);",
            "List<ResultSheet> findByAssignmentTeacherIdAndTermId(Long teacherId, Long termId);",
            "long countByStatus(ResultSheetStatus status);",
                    "List<ResultSheet> findByAssignmentClassRoomIdAndTermIdAndStatus(Long classId, Long termId, ResultSheetStatus status);",
        ],
    ),
    dict(
        name="Score", domain="results", table="scores", path="scores",
        plural="Scores", crud=False,
        fields=[
            f("ca", "Double", range=True),
            f("exam", "Double", range=True),
            f("total", "Double", range=True),
            f("remark", "String", search=True),
        ],
        relations=[
            r("sheet", "ResultSheet", null=False, ondelete="CASCADE",
              filter=True),
            r("student", "Student", null=False, ondelete="CASCADE",
              filter=True),
            r("grade", "GradeScale", null=True, ondelete="SET_NULL"),
        ],
        unique_together=[("sheet", "student")],
        extras=[("studentName", "student.user.fullName"),
                ("studentNo", "student.studentNo"),
                ("gradeCode", "grade.grade")],
        repo_methods=[
            "List<Score> findBySheetId(Long sheetId);",
            "Optional<Score> findBySheetIdAndStudentId(Long sheetId, Long studentId);",
            "List<Score> findByStudentId(Long studentId);",
            "List<Score> findBySheetIdIn(List<Long> sheetIds);",
        ],
    ),
    dict(
        name="GradeScale", domain="results", table="grade_scales",
        path="grading-scale", plural="GradeScales", crud=True,
        cache_evict=["gradeScaleByTotal"],
        fields=[
            f("grade", "String", req=True, uniq=True, search=True),
            f("minScore", "Double", req=True, range=True),
            f("maxScore", "Double", req=True, range=True),
            f("remark", "String", req=True, search=True),
        ],
        relations=[],
    ),
    dict(
        name="TermReport", domain="results", table="term_reports",
        path="term-reports", plural="TermReports", crud=False,
        fields=[
            f("average", "Double", range=True),
            f("classPosition", "Integer"),
            f("classSize", "Integer"),
            f("formTeacherRemark", "String", text=True),
        ],
        relations=[
            r("student", "Student", null=False, ondelete="CASCADE",
              filter=True),
            r("term", "Term", null=False, ondelete="CASCADE", filter=True),
            r("remarkBy", "Staff", null=True, ondelete="SET_NULL"),
        ],
        unique_together=[("student", "term")],
        extras=[("studentName", "student.user.fullName"),
                ("studentNo", "student.studentNo"),
                ("termName", "term.name"),
                ("sessionLabel", "term.session.label"),
                ("remarkByName", "remarkBy.user.fullName")],
        repo_methods=[
            "Optional<TermReport> findByStudentIdAndTermId(Long studentId, Long termId);",
            "List<TermReport> findByTermId(Long termId);",
            "List<TermReport> findByStudentId(Long studentId);",
        ],
    ),
    # ------------------------------------------------------------ campus life
    dict(
        name="Attendance", domain="campus", table="attendance",
        path="attendance", plural="AttendanceRecords", crud=False,
        fields=[
            f("date", "LocalDate", req=True, range=True, filter=True),
            f("status", "AttendanceStatus", req=True, default="PRESENT",
              filter=True),
        ],
        relations=[
            r("student", "Student", null=False, ondelete="CASCADE",
              filter=True),
            r("term", "Term", null=False, ondelete="CASCADE", filter=True),
        ],
        unique_together=[("student", "date")],
        extras=[("studentName", "student.user.fullName"),
                ("studentNo", "student.studentNo")],
        repo_methods=[
            "List<Attendance> findByStudentIdAndTermId(Long studentId, Long termId);",
            "List<Attendance> findByTermIdAndStudentIdIn(Long termId, List<Long> studentIds);",
            "Optional<Attendance> findByStudentIdAndDate(Long studentId, java.time.LocalDate date);",
        ],
    ),
    dict(
        name="Announcement", domain="campus", table="announcements",
        path="announcements", plural="Announcements", crud=True,
        cache_evict=["announcementFeed"],
        fields=[
            f("title", "String", req=True, search=True),
            f("content", "String", req=True, text=True, search=True),
            f("audience", "AnnouncementAudience", req=True, default="ALL",
              filter=True),
            f("eventAt", "LocalDateTime", range=True),
        ],
        relations=[r("createdBy", "User", null=True, ondelete="SET_NULL")],
        extras=[("createdByName", "createdBy.fullName")],
        repo_methods=[
            "List<Announcement> findByAudienceInOrderByCreatedAtDesc(List<AnnouncementAudience> audiences);",
        ],
    ),
    dict(
        name="SupportTicket", domain="campus", table="support_tickets",
        path="support", plural="SupportTickets", crud=True, open_create=True,
        fields=[
            f("issueType", "String", req=True, filter=True, search=True),
            f("message", "String", req=True, text=True, search=True),
            f("status", "TicketStatus", req=True, default="OPEN", filter=True),
        ],
        relations=[r("user", "User", null=False, ondelete="CASCADE")],
        extras=[("userName", "user.fullName"), ("userEmail", "user.email")],
        repo_methods=[
            "long countByStatus(TicketStatus status);",
        ],
    ),
    # -------------------------------------------------------- admin & system
    dict(
        name="SchoolSettings", domain="admin", table="school_settings",
        path="settings", plural="Settings", crud=False,
        fields=[
            f("name", "String", req=True, search=True),
            f("email", "String"),
            f("phone", "String"),
            f("website", "String"),
            f("address", "String", text=True),
            f("logoUrl", "String"),
            f("departmentsEnabled", "Boolean", req=True, default="true"),
            f("feesEnabled", "Boolean", req=True, default="true"),
            f("notificationPrefs", "String", text=True),
        ],
        relations=[r("currentTerm", "Term", null=True, ondelete="SET_NULL")],
        extras=[("currentTermName", "currentTerm.name"),
                ("currentSessionLabel", "currentTerm.session.label")],
        repo_methods=[
            "Optional<SchoolSettings> findFirstByOrderByIdAsc();",
        ],
    ),
    dict(
        name="AuditLog", domain="admin", table="audit_logs", path="audit-logs",
        plural="AuditLogs", crud=False,
        fields=[
            f("action", "String", req=True, search=True, filter=True),
            f("entityType", "String", req=True, search=True, filter=True),
            f("entityId", "Long"),
            f("summary", "String", text=True, search=True),
        ],
        relations=[r("actor", "User", null=True, ondelete="SET_NULL")],
        extras=[("actorName", "actor.fullName")],
        repo_methods=[
            "List<AuditLog> findTop15ByOrderByCreatedAtDesc();",
        ],
    ),
    dict(
        name="FeePayment", domain="admin", table="fee_payments", path="fees",
        plural="FeePayments", crud=False,
        fields=[
            f("amount", "Double", req=True, range=True),
            f("paidOn", "LocalDate", req=True, default="today", range=True),
            f("reference", "String", req=True, uniq=True, search=True),
        ],
        relations=[
            r("student", "Student", null=False, ondelete="CASCADE",
              filter=True),
            r("term", "Term", null=False, ondelete="CASCADE", filter=True),
        ],
        extras=[("studentName", "student.user.fullName"),
                ("studentNo", "student.studentNo")],
        module_gate="fees",
        repo_methods=[
            "List<FeePayment> findByStudentId(Long studentId);",
            "List<FeePayment> findByTermId(Long termId);",
            "List<FeePayment> findByStudentIdAndTermId(Long studentId, Long termId);",
        ],
    ),
    dict(
        name="LeaveRequest", domain="admin", table="leave_requests",
        path="leave", plural="LeaveRequests", crud=False,
        fields=[
            f("fromDate", "LocalDate", req=True, range=True),
            f("toDate", "LocalDate", req=True, range=True),
            f("reason", "String", req=True, text=True, search=True),
            f("status", "LeaveStatus", req=True, default="PENDING",
              filter=True),
        ],
        relations=[r("staff", "Staff", null=False, ondelete="CASCADE",
                     filter=True)],
        extras=[("staffName", "staff.user.fullName"),
                ("staffNo", "staff.staffNo")],
        repo_methods=[
            "List<LeaveRequest> findByStatus(LeaveStatus status);",
            "long countByStatus(LeaveStatus status);",
        ],
    ),
]

# Entities written by hand (they carry password hashing, workflow or single-row
# semantics); the generator still needs to know where they live.
HAND_WRITTEN = {
    "User": ("identity", "users"),
}

# Every enum lives beside the entity that owns it.
ENUM_PACKAGE = {
    "Role": "user",
    "StaffRole": "staff", "EmploymentType": "staff",
    "StudentStatus": "student", "Gender": "student",
    "TermName": "term",
    "Level": "classroom", "SubjectLevel": "subject",
    "DayOfWeek": "timetableSlot",
    "ResultSheetStatus": "resultSheet",
    "AttendanceStatus": "attendance", "AnnouncementAudience": "announcement",
    "TicketStatus": "supportTicket",
    "LeaveStatus": "leaveRequest",
}

DOMAIN_OF = {e["name"]: e["domain"] for e in ENTITIES}
DOMAIN_OF.update({name: d for name, (d, _) in HAND_WRITTEN.items()})
