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

        List<Report> reportList = reportService.findReportsByEmployeeCode(employeeCode);

        for (Report aReport : reportList) {
            if (aReport.getReportDate().equals(report.getReportDate())) {

                /*更新の時には個々のもう１処理　｛if (report.getId() != aReport.getId())｝　あったが、新登録の場合にはいらない。
                 * 更新の場合は既存のreportのリスト内にあるものと被った際に、今まさに更新しようとしているreportの可能性があったが、
                 * 新規の場合は今新しく作ってsaveできるかのチェックをしている段階のため、今表示しているrecordのidと同じreportがまだリスト内にあるはずがないため、
                 * そもそも日付がすでに登録されているreportのlistの中のものと被った時点で登録できない。 */

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

    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model, Report report,
            @AuthenticationPrincipal UserDetail userDetail) {

        // ここでnullが必要なのはemployeeのほうと同じ理由
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
            Model model, @PathVariable int id) {

        String employeeCode = userDetail.getUsername();
        Employee employee = employeeService.findByCode(employeeCode);
        List<Report> reportList = reportService.findReportsByEmployeeCode(employeeCode);

        // 入力した日付の日報データが存在する場合のエラー処理
        for (Report aReport : reportList) {
            if (aReport.getReportDate().equals(report.getReportDate())) {
                // ※更新画面で表示中の日報データは除く。(選択している日付さえも更新できなくなってしまうから)
                // つまり、下のエラー処理は処理対象の日付が、表示中の日付でない場合に実行することになる。
                /* ちなみにこの処理は主キーであるidを比較している。一意のはずだから、日付が一緒だったとしても、ここで一致すれば表示中の日報ということになるから処理が通る。
                   ここで一致しなければ、主キーの値が違うということで、現在表示中以外のどこかの日報と日付が同一であることになるためエラー処理が返される*/
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

        // ------登録日時はそのままにする-----------------------------------------

        LocalDateTime create = reportService.findById(id).getCreatedAt();
        report.setCreatedAt(create);

        reportService.updateReport(report);
        // ------------------------------------------------------------------

        return "redirect:/report";

    }

    // 日報削除
    @PostMapping(value = "{id}/delete")
    public String delete(@PathVariable int id) {
        reportService.reportDelete(id);

        return "redirect:/report";

    }
}
