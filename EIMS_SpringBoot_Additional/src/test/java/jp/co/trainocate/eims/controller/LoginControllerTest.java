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

    private Employee employee(String password) {
        return new Employee(10003, "山田", "太郎", "ヤマダ", "タロウ",
                password, 1, new Department(100, "人事部"), 0, 0);
    }
}
