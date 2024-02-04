package com.techacademy.service;

import java.util.List;

import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> findAll(){
        return reportRepository.findAll();
    }


}
