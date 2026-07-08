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
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final EmployeeService employeeService;

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
        if (result.hasErrors()) {
            return "login";
        }

        // 認証ロジック：社員番号で検索し、パスワードが一致するか確認
        Employee employee = employeeService.findById(loginForm.getEmpNo());
        
        if (employee != null) {
            if (employee.getPassword().equals(loginForm.getPassword()) && employee.getDeleteFlg() == 0) {
                // 認証成功：セッションにユーザー情報を保存
                session.setAttribute("loginEmployee", employee);
                return "redirect:/index";
            }
        }

        // 認証失敗
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
