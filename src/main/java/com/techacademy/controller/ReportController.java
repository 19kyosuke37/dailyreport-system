package com.techacademy.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Employee.Role;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("report")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;

    public ReportController(ReportService reportService,EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService =employeeService;
    }


    //ここを権限別に変える必要がある
    //日報一覧
    @GetMapping
    public String list(Model model,@AuthenticationPrincipal UserDetail userDetail) {
        Role userRole = userDetail.getRole();
        List<Report> reportList;
        if(userRole == Role.ADMIN) {
            reportList = reportService.findAll();
        }else{
            reportList = reportService.findReportsByEmployeeCode(userDetail.getUsername());
        }
            model.addAttribute("reportList", reportList);
            model.addAttribute("listSize", reportList.size());

        return "report/list";
    }

    // 日報詳細
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("report", reportService.findById(id));
        return "report/detail";

    }

    //新規登録
    @GetMapping(value = "/add")
    public String create(Model model,@ModelAttribute Report report,@AuthenticationPrincipal UserDetail userDetail) {
        model.addAttribute("name", userDetail.getName());
        return "report/new";
    }

    @PostMapping(value = "/add")
    public String add(@Validated Report report,@AuthenticationPrincipal UserDetail userDetail, BindingResult res,Model model) {

        if(res.hasErrors()) {
            return create(model,report,userDetail); //メソッドの定義と呼び出しの間で引数の並び順が一致している必要がある。
        }

        String employeeCode = userDetail.getUsername();
        Employee employee = employeeService.findByCode(employeeCode);
        report.setEmployeeCode(employee);


        reportService.saveReport(report);
        return "redirect:/report";

    }
}


