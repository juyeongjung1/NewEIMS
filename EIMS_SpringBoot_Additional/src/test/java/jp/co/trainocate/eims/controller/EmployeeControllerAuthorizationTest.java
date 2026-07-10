package jp.co.trainocate.eims.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("未ログインでは退職者管理画面を表示できない")
    void retireeListRequiresLogin() throws Exception {
        mockMvc.perform(get("/retireeList"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        verify(employeeService, never()).findRetirees();
    }

    @Test
    @DisplayName("一般ユーザーでは全社員一覧を表示できない")
    void employeeListRequiresAdmin() throws Exception {
        mockMvc.perform(get("/employeeList")
                .sessionAttr("loginEmployee", employee(10003, 0)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        verify(employeeService, never()).findAll();
    }

    @Test
    @DisplayName("管理者は全社員一覧を表示できる")
    void adminCanOpenEmployeeList() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/employeeList")
                .sessionAttr("loginEmployee", employee(10001, 1)))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_list"));
    }

    @Test
    @DisplayName("一般ユーザーは本人の変更画面を表示できる")
    void generalUserCanChangeSelf() throws Exception {
        Employee employee = employee(10003, 0);
        when(employeeService.findById(10003)).thenReturn(employee);
        when(departmentService.findAll()).thenReturn(List.of(employee.getDepartment()));

        mockMvc.perform(get("/changeInput/10003")
                .sessionAttr("loginEmployee", employee))
                .andExpect(status().isOk())
                .andExpect(view().name("change"));
    }

    @Test
    @DisplayName("一般ユーザーは他社員の変更画面を表示できない")
    void generalUserCannotChangeOtherEmployee() throws Exception {
        mockMvc.perform(get("/changeInput/10001")
                .sessionAttr("loginEmployee", employee(10003, 0)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        verify(employeeService, never()).findById(10001);
    }

    @Test
    @DisplayName("一般ユーザーは退職者を完全削除できない")
    void generalUserCannotPhysicallyDelete() throws Exception {
        mockMvc.perform(post("/physicalDelete/10002")
                .sessionAttr("loginEmployee", employee(10003, 0)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        verify(employeeService, never()).physicalDeleteById(10002);
    }

    @Test
    @DisplayName("管理者でも在籍社員を完全削除できない")
    void adminCannotPhysicallyDeleteActiveEmployee() throws Exception {
        when(employeeService.findById(10003)).thenReturn(employee(10003, 0));

        mockMvc.perform(post("/physicalDelete/10003")
                .sessionAttr("loginEmployee", employee(10001, 1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retireeList"));

        verify(employeeService, never()).physicalDeleteById(10003);
    }

    @Test
    @DisplayName("管理者本人は自分を退職処理できない")
    void adminCannotDeleteSelf() throws Exception {
        mockMvc.perform(post("/deleteEmployee")
                .param("empNo", "10001")
                .sessionAttr("loginEmployee", employee(10001, 1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        verify(employeeService, never()).deleteById(10001);
    }

    private Employee employee(Integer empNo, Integer role) {
        Department department = new Department(100, "人事部");
        return new Employee(empNo, "山田", "太郎", "ヤマダ", "タロウ",
                "password", 1, department, role, 0);
    }
}
