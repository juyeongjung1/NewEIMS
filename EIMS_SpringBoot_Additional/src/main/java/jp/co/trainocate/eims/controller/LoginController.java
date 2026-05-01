package jp.co.trainocate.eims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.LoginForm;
import jp.co.trainocate.eims.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class LoginController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * ログイン画面を表示します。
     */
    @GetMapping("/login")
    public String showLogin(@ModelAttribute LoginForm loginForm) {
        return "login";
    }

    /**
     * ログイン認証処理を行います。
     */
    @PostMapping("/login")
    public String login(@Validated @ModelAttribute LoginForm loginForm, BindingResult result, 
                        HttpSession session, Model model) {
        
        System.out.println("Login attempt for empNo: " + loginForm.getEmpNo());

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            return "login";
        }

        // 認証ロジック：社員番号で検索し、パスワードが一致するか確認
        Employee employee = employeeService.findById(loginForm.getEmpNo());
        
        if (employee != null) {
            System.out.println("Employee found: " + employee.getLastName());
            System.out.println("Password match: " + employee.getPassword().equals(loginForm.getPassword()));
            System.out.println("Delete flag: " + employee.getDeleteFlg());
            
            if (employee.getPassword().equals(loginForm.getPassword()) && employee.getDeleteFlg() == 0) {
                // 認証成功：セッションにユーザー情報を保存
                session.setAttribute("loginEmployee", employee);
                System.out.println("Login success. Redirecting to /index");
                return "redirect:/index";
            }
        } else {
            System.out.println("Employee not found for empNo: " + loginForm.getEmpNo());
        }

        // 認証失敗
        System.out.println("Login failed.");
        model.addAttribute("loginError", true);
        return "login";
    }

    /**
     * ログアウト処理を行います。
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // セッション破棄
        return "redirect:/login";
    }
}
