package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EmployeeService employeeService;

    public ReportService(ReportRepository reportRepository, EmployeeService employeeService) {
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
        // 上でoptionに代入する値が取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;

    }

    // 日報新規登録
    @Transactional
    public Report saveReport(Report report) {

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        /*
         * auto-increment（自動増分）として設定されているフィールド(今回はid)がインスタンスに割り当てられるのは
         * Repositoryのsaveメソッドなどで処理されたタイミング つまりこの下のメソッド！
         */

        return reportRepository.save(report);
    }

    // 日報更新
    @Transactional
    public Report updateReport(Report report) {

        LocalDateTime now = LocalDateTime.now();

        report.setUpdatedAt(now);

        return reportRepository.save(report);
    }

    // 日報削除
    @Transactional
    public void reportDelete(int id) {
        Report report = findById(id);
        report.setDeleteFlg(true);
    }

}
