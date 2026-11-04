package jp.co.trainocate.eims.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerSearchValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private DepartmentService departmentService;

    @ParameterizedTest
    @CsvSource(value = {
            "/selectByEmpNo|empNo||社員番号を入力してください。",
            "/selectByEmpNo|empNo|''|社員番号を入力してください。",
            "/selectByEmpNo|empNo|' '|社員番号を入力してください。",
            "/selectByEmpNo|empNo|'　'|社員番号を入力してください。",
            "/selectByEmpName|keyword||社員名を入力してください。",
            "/selectByEmpName|keyword|''|社員名を入力してください。",
            "/selectByEmpName|keyword|' '|社員名を入力してください。",
            "/selectByEmpName|keyword|'　'|社員名を入力してください。",
            "/selectByDeptNo|deptNo||部署を選択してください。",
            "/selectByDeptNo|deptNo|''|部署を選択してください。",
            "/selectByDeptNo|deptNo|' '|部署を選択してください。",
            "/selectByDeptNo|deptNo|'　'|部署を選択してください。"
    }, delimiter = '|')
    @DisplayName("各検索の未入力を通知し、別の検索条件には切り替えない")
    void missingConditionShowsMessage(String path, String parameter, String value, String message) throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(new Department(100, "人事部")));
        var request = get(path);
        if (value != null) {
            request.param(parameter, value);
        }
        if (!parameter.equals("empNo")) {
            request.param("empNo", "10001");
        }
        if (!parameter.equals("keyword")) {
            request.param("keyword", "山");
        }
        if (!parameter.equals("deptNo")) {
            request.param("deptNo", "100");
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("message", message))
                .andExpect(model().attribute("departments", hasSize(1)))
                .andExpect(content().string(containsString(message)));
        verifyNoInteractions(employeeService);
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 256})
    @DisplayName("氏名検索は11文字・256文字を受け付け、前後の空白を除去しない")
    void nameSearchAcceptsLongKeywordsWithoutTrimming(int length) throws Exception {
        String keyword = " " + "あ".repeat(length - 2) + " ";
        when(employeeService.findByEmpName(keyword)).thenReturn(List.of());

        mockMvc.perform(get("/selectByEmpName").param("keyword", keyword).param("empNo", "10001"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_result"))
                .andExpect(model().attribute("employees", hasSize(0)));
        verify(employeeService).findByEmpName(keyword);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    @DisplayName("部署検索は別の検索条件があっても指定部署の検索結果を表示する")
    void departmentSearchIgnoresOtherConditions() throws Exception {
        when(employeeService.findByDeptNo(100)).thenReturn(List.of());

        mockMvc.perform(get("/selectByDeptNo").param("deptNo", "100").param("empNo", "10001")
                .param("keyword", "山"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_result"))
                .andExpect(model().attribute("employees", hasSize(0)));
        verify(employeeService).findByDeptNo(100);
        verifyNoMoreInteractions(employeeService);
    }

    @ParameterizedTest
    @CsvSource({"/selectByEmpNo, empNo, abc", "/selectByEmpNo, empNo, 2147483648",
            "/selectByDeptNo, deptNo, abc", "/selectByDeptNo, deptNo, 1.5"})
    @DisplayName("番号の型変換に失敗した場合は従来どおり400を返す")
    void invalidNumberReturnsBadRequest(String path, String parameter, String value) throws Exception {
        mockMvc.perform(get(path).param(parameter, value))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(employeeService);
    }

}
