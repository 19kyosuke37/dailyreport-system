package com.techacademy.controller;

import java.time.LocalDateTime;
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

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
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

    public ReportController(ReportService reportService, EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
    }

    // ここを権限別に変える必要がある
    // 日報一覧
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetail userDetail) {
        Role userRole = userDetail.getRole();
        List<Report> reportList;
        if (userRole == Role.ADMIN) {
            reportList = reportService.findAll();
        } else {
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

    // 新規登録
    @GetMapping(value = "/add")
    public String create(Model model, @ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail) {
        model.addAttribute("name", userDetail.getName());
        return "report/new";
    }

    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail,
            Model model) {

        String employeeCode = userDetail.getUsername();
        Employee employee = employeeService.findByCode(employeeCode);

        // ----------- すでに登録したい日付が、同じユーザーで登録されている場合のエラー----------------------------

        List<Report> reportList = reportService.findReportsByEmployeeCode(userDetail.getUsername());

        for (Report aReport : reportList) {
            if (aReport.getReportDate().equals(report.getReportDate())) {

                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));

                return create(model, report, userDetail);
            }
        }

        // --------------------------------------------------------------------------------

        if (res.hasErrors()) {
            return create(model, report, userDetail); // メソッドの定義と呼び出しの間で引数の並び順が一致している必要がある。
        }

        report.setEmployeeCode(employee);

        reportService.saveReport(report);
        return "redirect:/report";

    }

    // 日報更新画面(追加)
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model, Report report,
            @AuthenticationPrincipal UserDetail userDetail) {

        if (id != null) {
            model.addAttribute("report", reportService.findById(id));
        } else {
            model.addAttribute("report", report);
        }

        model.addAttribute("name", userDetail.getName());

        return "report/update";
    }

    @PostMapping(value = "/{id}/update")
    public String update(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail,
            Model model,@PathVariable int id) {

        String employeeCode = userDetail.getUsername();
        Employee employee = employeeService.findByCode(employeeCode);
        List<Report> reportList = reportService.findReportsByEmployeeCode(userDetail.getUsername());

        for (Report aReport : reportList) {
            if (aReport.getReportDate().equals(report.getReportDate())) {

                if (report.getId() != aReport.getId()) {

                    model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                            ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));

                    return create(model, report, userDetail);
                }
            }
        }

        if (res.hasErrors()) {
            return edit(null, model, report, userDetail); // メソッドの定義と呼び出しの間で引数の並び順が一致している必要がある。
        }

        report.setEmployeeCode(employee);

        //------登録日時はそのままにする-----------------------------------------

        LocalDateTime create =reportService.findById(id).getCreatedAt();
        report.setCreatedAt(create);

        reportService.updateReport(report);
        //------------------------------------------------------------------

        return "redirect:/report";

    }

    // 日報削除
    @PostMapping(value = "{id}/delete")
    public String delete(@PathVariable int id) {
        reportService.reportDelete(id);

        return "redirect:/report";

    }
}
