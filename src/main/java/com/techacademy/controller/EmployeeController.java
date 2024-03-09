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

    /*
     * //従業員検索
     *
     * @GetMapping
     * public String search(Model model,) {
     *
     * return "employees/list"; }
     *
     */

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {

        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());

        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    // valueってのはあってもなくてもいい
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

        // ★★★論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応★★★
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            // サービスにて半角英数字チェックやDB登録処理を行い、結果をErrorKindsクラスで受け取る。
            ErrorKinds result = employeeService.save(employee);

            // 結果の値によってエラーメッセージを判定し、モデルへ設定した後でcreateメソッドへ遷移する。
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

            // 従業員IDが重複していた場合のエラー判定
        } catch (DataIntegrityViolationException e) {
            // ErrorMessageクラスのgetErrorNameメソッドを使用して「"codeError",
            // "既に登録されている社員番号です"」をモデルへ設定する。その後createメソッドへ遷移する。
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
 //★いろんなところでつかっている (ErrorMessage.contains(result))はようはserviceから返ってきたreturnが"ErrorMessage.SUCSECS"じゃなかった場合のこと
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

    // 従業員更新画面
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable String code, Model model, Employee employee) {

        /*
         * 下の処理について、 存在する従業員の詳細ページから送られてくるのに、nullなはずがないと思っていたが、
         * この後の更新処理時にpostメソッドで送られてきたものが、バリデーションでエラーがあり再度editメソッドに返された際には
         * コードがnullなため、エラーがあったemplyeeの値でmodelを扱う必要があった！
         */
        if (code != null) {
            model.addAttribute("employee", employeeService.findByCode(code));
        } else {
            model.addAttribute("employee", employee);
        }

        return "employees/update";
    }

    // 従業員更新処理(追加）
    @PostMapping(value = "/{code}/update")
    public String update(@Validated Employee employee, BindingResult res, Model model, @PathVariable String code) {

        // 空欄でないかつ、文字数が8文字未満16文字を超えない、半角以外の英数字を使っていない//

        ErrorKinds result = employeeService.employeePasswordCheck(employee);

        if (!"".equals(employee.getPassword()) && ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return edit(null, model, employee);//ここで引数にされるemployeeはまちがった値がフィールドに設定されたもの
        }

        //ここまで処理が到達したら、とりあえずパスワードでのエラーはなかったよーってことになる

        // 入力チェック（エンティティで設定したバリデーションチェック）
        if (res.hasErrors()) {
            return edit(null, model, employee);//ここでも引数にされるemployeeはまちがった値がフィールドに設定されたもの
        }

        // ------登録日時はそのままにする(場所に注意。上から順番にプログラムが実行されることを意識する）------
        /*
         * ～～復習～～ 最初にErrorKinds updateResult =
         * employeeService.update(employee)より下に書いていたから、エラーが起きた それより下だと、
         * employeeRepository.save(employee);より後に実行されることになるから、CreatedAtがnullのままで
         * 更新データを保存することになるからエラーになる
         */
        LocalDateTime create = employeeService.findByCode(code).getCreatedAt();
        employee.setCreatedAt(create);
        // --------------------------------------------------------------------------------

        ErrorKinds updateResult = employeeService.update(employee);

        if (ErrorMessage.contains(updateResult)) {
            model.addAttribute(ErrorMessage.getErrorName(updateResult), ErrorMessage.getErrorValue(updateResult));
            return edit(null, model, employee);
        }

        return "redirect:/employees";
    }

}
