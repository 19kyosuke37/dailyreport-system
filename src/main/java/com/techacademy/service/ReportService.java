package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EmployeeService employeeService;

    public ReportService(ReportRepository reportRepository,EmployeeService employeeService) {
        this.reportRepository = reportRepository;
        this.employeeService = employeeService;
    }

    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 社員番号に基づいて日報を取得する
    public List<Report> findReportsByEmployeeCode(String employeeCode) {

        return reportRepository.findByEmployeeCode_Code(employeeCode);
    }

    public Report findById(int id) {
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;

    }

    public Report saveReport(Report report) {

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        return reportRepository.save(report);
    }

}
