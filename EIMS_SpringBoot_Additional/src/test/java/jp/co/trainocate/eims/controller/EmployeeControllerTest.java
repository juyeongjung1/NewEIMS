package jp.co.trainocate.eims.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

/**
 * 追加機能⑤（詳細画面・画面遷移改善／設計書05 §10）および
 * ③（退職者一覧の導線／設計書03）の受け入れ条件を検証するコントローラテスト。
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    // EmployeeController のコンストラクタ依存を満たすためモック化（本テストでは未使用）
    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("詳細画面：存在しない社員番号は検索画面へ戻る（NPE回帰防止）")
    void detailRedirectsToSearchWhenEmployeeMissing() throws Exception {
        when(employeeService.findById(99999)).thenReturn(null);

        mockMvc.perform(get("/detail/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search"));
    }

    @Test
    @DisplayName("詳細画面：存在する社員番号は詳細画面を表示する")
    void detailShowsEmployeeWhenFound() throws Exception {
        when(employeeService.findById(10001)).thenReturn(activeEmployee(10001));

        mockMvc.perform(get("/detail/10001"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_detail"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    @DisplayName("変更画面：存在しない社員番号は検索画面へ戻る（NPE回帰防止）")
    void changeInputRedirectsToSearchWhenEmployeeMissing() throws Exception {
        when(employeeService.findById(99999)).thenReturn(null);

        mockMvc.perform(get("/changeInput/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search"));
    }

    @Test
    @DisplayName("社員番号検索：在籍者にヒットすると詳細画面へ直行する（画面遷移改善）")
    void empNoSearchHitGoesDirectlyToDetail() throws Exception {
        when(employeeService.findById(10001)).thenReturn(activeEmployee(10001));

        mockMvc.perform(get("/selectByEmpNo").param("empNo", "10001"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_detail"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    @DisplayName("社員番号検索：ヒットしない場合は検索結果画面（0件）を表示する")
    void empNoSearchMissGoesToSearchResult() throws Exception {
        when(employeeService.findById(99999)).thenReturn(null);

        mockMvc.perform(get("/selectByEmpNo").param("empNo", "99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_result"));
    }

    @Test
    @DisplayName("社員番号検索：退職者は詳細直行せず検索結果画面（0件）へ")
    void empNoSearchRetireeGoesToSearchResult() throws Exception {
        Employee retiree = activeEmployee(10019);
        retiree.setDeleteFlg(1);
        when(employeeService.findById(10019)).thenReturn(retiree);

        mockMvc.perform(get("/selectByEmpNo").param("empNo", "10019"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_result"));
    }

    @Test
    @DisplayName("退職者一覧：退職者リストを retirees としてビューに渡す")
    void retireeListShowsRetirees() throws Exception {
        Employee retiree = activeEmployee(10019);
        retiree.setDeleteFlg(1);
        when(employeeService.findRetirees()).thenReturn(List.of(retiree));

        mockMvc.perform(get("/retireeList"))
                .andExpect(status().isOk())
                .andExpect(view().name("retiree_list"))
                .andExpect(model().attributeExists("retirees"));
    }

    @Test
    @DisplayName("復元：処理後は退職者一覧へリダイレクトする")
    void restoreRedirectsToRetireeList() throws Exception {
        mockMvc.perform(post("/restore/10019"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retireeList"));

        verify(employeeService).restoreById(10019);
    }

    @Test
    @DisplayName("完全削除：処理後は退職者一覧へリダイレクトする")
    void physicalDeleteRedirectsToRetireeList() throws Exception {
        mockMvc.perform(post("/physicalDelete/10019"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retireeList"));

        verify(employeeService).physicalDeleteById(10019);
    }

    /** テスト用の在籍社員（delete_flg=0）を生成する。 */
    private Employee activeEmployee(Integer empNo) {
        Employee employee = new Employee();
        employee.setEmpNo(empNo);
        employee.setLastName("山田");
        employee.setFirstName("太郎");
        employee.setLastKana("ヤマダ");
        employee.setFirstKana("タロウ");
        employee.setPassword("password");
        employee.setGender(1);
        employee.setDeptNo(100);
        employee.setDepartment(new Department(100, "営業部"));
        employee.setRole(0);
        employee.setDeleteFlg(0);
        return employee;
    }
}
