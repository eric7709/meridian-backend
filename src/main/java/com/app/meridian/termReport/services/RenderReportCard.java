package com.app.meridian.termReport.services;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.schoolSettings.SchoolSettingsRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.student.Student;
import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.termReport.TermReportRepository;
import com.app.meridian.student.dto.StudentSubjectScoreDTO;
import com.app.meridian.student.dto.StudentTermResultDTO;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;
import com.app.meridian.security.SecurityUtils;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.student.services.GetStudentResults;
import com.app.meridian.subject.Subject;

/** The downloadable report card: header, subject table, average, position and remark. */
@Service
@RequiredArgsConstructor
public class RenderReportCard {

    private final GetCurrentStudent currentStudent;
    private final GetStudentResults getStudentResults;
    private final TermRepository termRepository;
    private final TermReportRepository termReportRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;

    @Transactional(readOnly = true)
    public byte[] render(Long studentId, Long termId) {
        boolean student = SecurityUtils.principal().isStudent();
        Long target = studentId != null ? studentId : currentStudent.get().getId();
        if (student && !target.equals(currentStudent.get().getId())) {
            throw new com.app.meridian.exceptions.ForbiddenException("You can only download your own report card");
        }
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));
        StudentTermResultDTO result = getStudentResults
                .get(target, term.getSession().getId(), term.getName().name())
                .terms().stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No approved results for that term yet"));
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndSessionId(target, term.getSession().getId()).orElse(null);
        String schoolName = schoolSettingsRepository.findFirstByOrderByIdAsc()
                .map(SchoolSettings::getName).orElse("Meridian");
        byte[] pdf;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, out);
            document.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph name = new Paragraph(schoolName, title);
            name.setAlignment(Element.ALIGN_CENTER);
            document.add(name);
            Paragraph subtitle = new Paragraph(term.getName().name() + " TERM REPORT — "
                    + term.getSession().getLabel(), heading);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16);
            document.add(subtitle);
            document.add(new Paragraph("Student: " + result.subjects().size() + " subjects", body));
            Paragraph identity = new Paragraph();
            identity.add("Name: " + nameOf(target) + "    ");
            identity.add("Student No: " + studentNoOf(target) + "    ");
            identity.add("Class: " + (enrollment == null ? "-" : enrollment.getClassRoom().getName()));
            identity.setSpacingAfter(12);
            document.add(identity);
            PdfPTable table = new PdfPTable(new float[] {4, 1.2f, 1.2f, 1.2f, 1.2f, 4});
            table.setWidthPercentage(100);
            for (String column : new String[] {"Subject", "CA (40)", "Exam (60)", "Total", "Grade", "Remark"}) {
                PdfPCell cell = new PdfPCell(new Paragraph(column, heading));
                cell.setPadding(6);
                table.addCell(cell);
            }
            for (StudentSubjectScoreDTO subject : result.subjects()) {
                table.addCell(new PdfPCell(new Paragraph(subject.subjectName(), body)));
                table.addCell(new PdfPCell(new Paragraph(text(subject.ca()), body)));
                table.addCell(new PdfPCell(new Paragraph(text(subject.exam()), body)));
                table.addCell(new PdfPCell(new Paragraph(text(subject.total()), body)));
                table.addCell(new PdfPCell(new Paragraph(subject.grade() == null ? "-" : subject.grade(), body)));
                table.addCell(new PdfPCell(new Paragraph(subject.remark() == null ? "-" : subject.remark(), body)));
            }
            document.add(table);
            Paragraph summary = new Paragraph();
            summary.add("Average: " + text(result.average()) + "    ");
            summary.add("Position: " + (result.classPosition() == null ? "-" : result.classPosition().toString()));
            summary.add(" of " + (result.classSize() == null ? "-" : result.classSize().toString()));
            summary.setSpacingBefore(14);
            document.add(summary);
            TermReport report = termReportRepository
                    .findByStudentIdAndTermId(target, term.getId()).orElse(null);
            if (report != null && report.getFormTeacherRemark() != null) {
                Paragraph remark = new Paragraph("Form teacher's remark: " + report.getFormTeacherRemark(), body);
                remark.setSpacingBefore(10);
                document.add(remark);
            }
            document.close();
            pdf = out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not render the report card", ex);
        }
        return pdf;
    }

    private String nameOf(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByEnrolledOnDesc(studentId).stream()
                .findFirst()
                .map(enrollment -> enrollment.getStudent().getUser().getFullName())
                .orElse("-");
    }

    private String studentNoOf(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByEnrolledOnDesc(studentId).stream()
                .findFirst()
                .map(enrollment -> enrollment.getStudent().getStudentNo())
                .orElse("-");
    }

    private String text(Double number) {
        return number == null ? "-" : String.valueOf(number);
    }
}
