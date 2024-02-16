package com.techacademy.controller;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {

        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());

        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    // valueってのはあってもなくてもいいと思う（lesson16とか参照）
    public String detail(@PathVariable String code, Model model) {

        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {

        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {

        // パスワード空白チェック
        /*
         * エンティティ側の入力チェックでも実装は行えるが、更新の方でパスワードが空白でもチェックエラーを出さずに
         * 更新出来る仕様となっているため上記を考慮した場合に別でエラーメッセージを出す方法が簡単だと判断
         */
        if ("".equals(employee.getPassword())) {
            // パスワードが空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(employee);

        }

        // 入力チェック
        if (res.hasErrors()) {
            return create(employee);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = employeeService.save(employee);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }

        return "redirect:/employees";
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = employeeService.delete(code, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

    // 以下追加

    // 従業員更新画面(追加)
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable String code, Model model, Employee employee) {

        if (code != null) {
            model.addAttribute("employee", employeeService.findByCode(code));
        } else {
            model.addAttribute("employee", employee);
        }

        return "employees/update";
    }

    // 従業員更新処理(追加）
    @PostMapping(value = "/{code}/update")
    public String update(@Validated Employee employee, BindingResult res, Model model,@PathVariable String code) {

        // 空白では無いかつ、文字数が8文字以上16文字以下でない
        int passwordLength = employee.getPassword().length();

        if (!"".equals(employee.getPassword()) && (passwordLength < 8 || 16 < passwordLength)) {

            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.RANGECHECK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.RANGECHECK_ERROR));

            return edit(null, model, employee);
        }

        // 空白では無いかつ、半角英数字以外を使用した場合
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());

        if (!"".equals(employee.getPassword()) && (!matcher.matches())) {

            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.HALFSIZE_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.HALFSIZE_ERROR));

            return edit(null, model, employee);
        }

        // 入力チェック
        if (res.hasErrors()) {
            return edit(null, model, employee);
        }


        //------登録日時はそのままにする(場所に注意。上から順番にプログラムが実行されることを意識する）------
        /*～～復習～～
         * 最初にErrorKinds result = employeeService.update(employee)より下に書いていたから、エラーが起きた
         * それより下だと、 employeeRepository.save(employee);より後に実行されることになるから、CreatedAtがnullのままでエラーになる*/
        LocalDateTime create = employeeService.findByCode(code).getCreatedAt();
        employee.setCreatedAt(create);
        //--------------------------------------------------------------------------------

        ErrorKinds result = employeeService.update(employee);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return edit(null, model, employee);
        }



        return "redirect:/employees";
    }

}
