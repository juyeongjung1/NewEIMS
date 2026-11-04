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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerSearchTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	private List<Department> deptList = List.of(
			new Department(100, "人事部"),
			new Department(200, "経理部"),
			new Department(300, "営業部"));

	private List<Employee> empList = List.of(
			new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0)),
			new Employee(10002, "中田", "結衣", "タナカ", "ユイ", "password", 2, deptList.get(0)),
			new Employee(10003, "鈴木", "大翔", "スズキ", "ヒロト", "password", 1, deptList.get(2)));

	@Test
	@DisplayName("社員番号検索（パラメータあり）：詳細を employee_detail に表示")
	void testSelectByEmpNo_WithParam_ReturnsResult() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		mockMvc.perform(get("/selectByEmpNo").param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_detail"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	@Test
	@DisplayName("社員番号検索（該当なし）：0件の search_result を表示")
	void testSelectByEmpNo_NotFound_ReturnsSearchResult() throws Exception {
		Mockito.when(employeeService.findById(99999)).thenReturn(null);

		mockMvc.perform(get("/selectByEmpNo").param("empNo", "99999"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}

	@Test
	@DisplayName("社員番号検索（パラメータ無し）：検索画面に戻る")
	void testSelectByEmpNo_NoParam_ReturnsSearch() throws Exception {
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/selectByEmpNo"))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	@Test
	@DisplayName("氏名キーワード検索：「田」の検索結果を search_result に表示")
	void testSelectByEmpName_ReturnsResult() throws Exception {
		List<Employee> mockEmployees = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0)),
				new Employee(10002, "中田", "結衣", "タナカ", "ユイ", "password", 2, deptList.get(0)));
		Mockito.when(employeeService.findByEmpName("田")).thenReturn(mockEmployees);

		mockMvc.perform(get("/selectByEmpName").param("keyword", "田"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)));
	}

	@Test
	@DisplayName("氏名検索（0件）：0件の search_result を表示")
	void testSelectByEmpName_NoResult_ReturnsSearchResult() throws Exception {
		Mockito.when(employeeService.findByEmpName("該当なし")).thenReturn(List.of());

		mockMvc.perform(get("/selectByEmpName").param("keyword", "該当なし"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}

	@Test
	@DisplayName("部門番号検索：結果を search_result に表示（件数も検証）")
	void testSelectByDeptNo_ReturnsResult() throws Exception {
		List<Employee> mockEmployee = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0)),
				new Employee(10002, "中田", "結衣", "タナカ", "ユイ", "password", 2, deptList.get(0)));
		Mockito.when(employeeService.findByDeptNo(100)).thenReturn(mockEmployee);

		mockMvc.perform(get("/selectByDeptNo").param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)));
	}

	@Test
	@DisplayName("社員詳細（/detail/{empNo}）：詳細を employee_detail に表示")
	void testShowDetail_ReturnsEmployeeDetail() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		mockMvc.perform(get("/detail/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_detail"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

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
