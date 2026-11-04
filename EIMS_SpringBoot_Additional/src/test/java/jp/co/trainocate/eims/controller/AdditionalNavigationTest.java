package jp.co.trainocate.eims.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest({EmployeeController.class, LoginController.class})
class AdditionalNavigationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("退職者一覧から詳細を確認し、退職者一覧へ戻れる")
    void retireeCanBeViewedFromListAndReturnToList() throws Exception {
        Employee retiree = employee(1);
        when(employeeService.findRetirees()).thenReturn(List.of(retiree));
        when(employeeService.findById(10019)).thenReturn(retiree);
        Employee administrator = employee(0);
        administrator.setEmpNo(10001);
        administrator.setRole(1);

        mockMvc.perform(get("/retireeList").sessionAttr("loginEmployee", administrator))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/detail/10019\"")));

        mockMvc.perform(get("/detail/10019").sessionAttr("loginEmployee", administrator))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<td>退職</td>")))
                .andExpect(content().string(containsString("<a href=\"/retireeList\" class=\"btn btn-outline-secondary\">戻る</a>")));
    }

    @Test
    @DisplayName("在籍社員の詳細画面は検索画面へ戻る")
    void activeEmployeeDetailReturnsToSearch() throws Exception {
        when(employeeService.findById(10019)).thenReturn(employee(0));

        mockMvc.perform(get("/detail/10019"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<td>在籍</td>")))
                .andExpect(content().string(not(containsString("href=\"/retireeList\""))))
                .andExpect(content().string(containsString("<a href=\"/search\" class=\"btn btn-outline-secondary\">戻る</a>")));
    }

    @Test
    @DisplayName("ログイン画面には社員登録へのリンクを表示しない")
    void loginHasNoRegistrationLink() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/input\""))))
                .andExpect(content().string(not(containsString("会員登録"))))
                .andExpect(content().string(containsString("action=\"/login\"")));
    }

    @Test
    @DisplayName("ログイン失敗時も社員登録へのリンクを表示しない")
    void failedLoginHasNoRegistrationLink() throws Exception {
        mockMvc.perform(post("/login").param("empNo", "99999").param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("社員番号またはパスワードが正しくありません。")))
                .andExpect(content().string(not(containsString("href=\"/input\""))));
    }

    @Test
    @DisplayName("削除対象が存在しない場合は検索0件とは別のメッセージを表示する")
    void missingDeleteTargetShowsDeletionMessage() throws Exception {
        mockMvc.perform(get("/deleteConfirm/99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("指定された社員情報は存在しないため、削除できません。")))
                .andExpect(content().string(not(containsString("該当する在籍社員は存在しませんでした。"))));

        mockMvc.perform(post("/deleteEmployee").param("empNo", "99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("指定された社員情報は存在しないため、削除できません。")))
                .andExpect(content().string(not(containsString("該当する在籍社員は存在しませんでした。"))));
    }

    @Test
    @DisplayName("検索結果0件の場合は在籍社員の0件メッセージを表示する")
    void noSearchResultsShowsSearchMessage() throws Exception {
        mockMvc.perform(get("/selectByEmpNo").param("empNo", "99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("該当する在籍社員は存在しませんでした。")))
                .andExpect(content().string(not(containsString("指定された社員情報は存在しないため、削除できません。"))));
    }

    @Test
    @DisplayName("退職確認画面に対象社員のカナと性別を表示する")
    void retirementConfirmationShowsKanaAndGender() throws Exception {
        when(employeeService.findById(10019)).thenReturn(employee(0));

        mockMvc.perform(get("/deleteConfirm/10019"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ヤマダ タロウ")))
                .andExpect(content().string(containsString("<td>男性</td>")));
    }

    private Employee employee(Integer deleteFlg) {
        Employee employee = new Employee();
        employee.setEmpNo(10019);
        employee.setLastName("山田");
        employee.setFirstName("太郎");
        employee.setLastKana("ヤマダ");
        employee.setFirstKana("タロウ");
        employee.setGender(1);
        employee.setDeptNo(100);
        employee.setDepartment(new Department(100, "営業部"));
        employee.setRole(0);
        employee.setDeleteFlg(deleteFlg);
        return employee;
    }
}
