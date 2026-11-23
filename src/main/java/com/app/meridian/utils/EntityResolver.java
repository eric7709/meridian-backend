package com.app.meridian.utils;

import org.springframework.stereotype.Component;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.classroom.ClassRoomRepository;
import com.app.meridian.subject.Subject;
import com.app.meridian.subject.SubjectRepository;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;
import com.app.meridian.department.Department;
import com.app.meridian.department.DepartmentRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.StaffRepository;
import com.app.meridian.student.Student;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleRepository;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;

import lombok.RequiredArgsConstructor;

/**
 * Turns the ids that arrive in a request body into entities. A null id resolves to null so that
 * optional relationships (department, class, current term) stay optional.
 */
@Component
@RequiredArgsConstructor
public class EntityResolver {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final TermRepository termRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ResultSheetRepository resultSheetRepository;
    private final GradeScaleRepository gradeScaleRepository;

    public User resolveUser(Long id) {
        return id == null ? null : userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    public Student resolveStudent(Long id) {
        return id == null ? null : studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student " + id + " not found"));
    }

    public Staff resolveStaff(Long id) {
        return id == null ? null : staffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staff " + id + " not found"));
    }

    public Department resolveDepartment(Long id) {
        return id == null ? null : departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department " + id + " not found"));
    }

    public AcademicSession resolveAcademicSession(Long id) {
        return id == null ? null : academicSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Academic session " + id + " not found"));
    }

    public Term resolveTerm(Long id) {
        return id == null ? null : termRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Term " + id + " not found"));
    }

    public ClassRoom resolveClassRoom(Long id) {
        return id == null ? null : classRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class " + id + " not found"));
    }

    public Subject resolveSubject(Long id) {
        return id == null ? null : subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject " + id + " not found"));
    }

    public TeachingAssignment resolveTeachingAssignment(Long id) {
        return id == null ? null : teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Teaching assignment " + id + " not found"));
    }

    public ResultSheet resolveResultSheet(Long id) {
        return id == null ? null : resultSheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Result sheet " + id + " not found"));
    }

    public GradeScale resolveGradeScale(Long id) {
        return id == null ? null : gradeScaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grade " + id + " not found"));
    }
}
