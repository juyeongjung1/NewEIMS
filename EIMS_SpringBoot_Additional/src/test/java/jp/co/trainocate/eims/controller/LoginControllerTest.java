package jp.co.trainocate.eims.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("共通機能と同じ4文字のパスワードでログインできる")
    void loginAcceptsFourCharacterPassword() throws Exception {
        Employee employee = employee("abcd");
        when(employeeService.findById(10003)).thenReturn(employee);

        mockMvc.perform(post("/login")
                .param("empNo", "10003")
                .param("password", "abcd"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andExpect(request().sessionAttribute("loginEmployee", employee));
    }

    @Test
    @DisplayName("17文字のパスワードは入力エラーになる")
    void loginRejectsSeventeenCharacterPassword() throws Exception {
        mockMvc.perform(post("/login")
                .param("empNo", "10003")
                .param("password", "12345678901234567"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "password"));
    }

    @Test
    @DisplayName("未入力の場合は入力エラーになる")
    void loginRejectsEmptyInput() throws Exception {
        mockMvc.perform(post("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "empNo", "password"));
    }

    @Test
    @DisplayName("3文字のパスワードは入力エラーになる")
    void loginRejectsThreeCharacterPassword() throws Exception {
        mockMvc.perform(post("/login")
                .param("empNo", "10003")
                .param("password", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "password"));
    }

    @Test
    @DisplayName("存在しない社員番号ではログインできない")
    void loginRejectsUnknownEmployee() throws Exception {
        when(employeeService.findById(99999)).thenReturn(null);

        mockMvc.perform(post("/login")
                .param("empNo", "99999")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginError", true));
    }

    @Test
    @DisplayName("パスワードが一致しない場合はログインできない")
    void loginRejectsMismatchedPassword() throws Exception {
        when(employeeService.findById(10003)).thenReturn(employee("password"));

        mockMvc.perform(post("/login")
                .param("empNo", "10003")
                .param("password", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginError", true));
    }

    @Test
    @DisplayName("退職済み社員はログインできない")
    void loginRejectsRetiredEmployee() throws Exception {
        Employee retiredEmployee = employee("password");
        retiredEmployee.setDeleteFlg(1);
        when(employeeService.findById(10003)).thenReturn(retiredEmployee);

        mockMvc.perform(post("/login")
                .param("empNo", "10003")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginError", true));
    }

    @Test
    @DisplayName("ログアウトするとセッションを破棄してログイン画面へ戻る")
    void logoutInvalidatesSession() throws Exception {
        mockMvc.perform(post("/logout").sessionAttr("loginEmployee", employee("password")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(request().sessionAttributeDoesNotExist("loginEmployee"));
    }

    private Employee employee(String password) {
        return new Employee(10003, "山田", "太郎", "ヤマダ", "タロウ",
                password, 1, new Department(100, "人事部"), 0, 0);
    }
}
