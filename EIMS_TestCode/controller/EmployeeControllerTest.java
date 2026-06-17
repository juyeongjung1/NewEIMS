package jp.co.trainocate.enshu.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.enshu.entity.Department;
import jp.co.trainocate.enshu.entity.Employee;
import jp.co.trainocate.enshu.service.DepartmentService;
import jp.co.trainocate.enshu.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private EmployeeService employeeService;

	@MockBean
	private DepartmentService departmentService;

	private List<Department> deptList = List.of(
			new Department(100, "人事部"),
			new Department(200, "経理部"),
			new Department(300, "営業部"));

	private List<Employee> empList = List.of(
			new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0)),
			new Employee(10002, "中田", "結衣", "タナカ", "ユイ", "password", 2, deptList.get(0)),
			new Employee(10003, "鈴木", "大翔", "スズキ", "ヒロト", "password", 1, deptList.get(2)));

	// ---------- GET /index ----------
	@Test
	@DisplayName("index画面の表示：index ビューが返る")
	void testIndex_ReturnsIndexView() throws Exception {
		mockMvc.perform(get("/index"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	// ---------- GET /search ----------
	@Test
	@DisplayName("検索画面の表示：部門リストをモデルに詰めて search を返す")
	void testShowSearchPage_ReturnsSearchWithDepartments() throws Exception {
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/search"))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));

	}

	// ---------- GET /selectByEmpNo?empNo=10001 ----------
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

	// ---------- GET /selectByEmpNo?empNo=99999 ----------
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

	// ---------- GET /selectByEmpNo（パラメータ無し） ----------
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

	// ---------- GET /selectByEmpName?keyword=結衣 ----------
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

	// ---------- GET /selectByEmpName?keyword=該当なし ----------
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

	// ---------- GET /selectByDeptNo?deptNo=100 ----------
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

	// ---------- GET /input ----------
	@Test
	@DisplayName("登録画面の表示：部門リストありで input を返す")
	void testShowInputPage_ReturnsInputWithDepartments() throws Exception {
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/input"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	// ---------- POST /inputConfirm（正常系） ----------
	@Test
	@DisplayName("登録確認(正常系)：バリデーションOKで input_confirm を返す（選択部門をモデルへ）")
	void testConfirmRegistration_WhenValid_ReturnsConfirm() throws Exception {
		Mockito.when(departmentService.findById(100)).thenReturn(deptList.get(0));

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input_confirm"))
				.andExpect(model().attributeExists("department"))
				.andExpect(model().attribute("department", hasProperty("deptName",
						is("人事部"))));

	}

	// ---------- POST /inputConfirm（NotNull&NotBlankバリデーションNG） ----------
	@Test
	@DisplayName("登録確認(異常系➀)：NotNull&NotBlankバリデーションNGで input に戻る（部門リスト存在）")
	void testConfirmRegistration_WhenValidationFails1_ReturnsInput() throws Exception {

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "")
				.param("firstName", "")
				.param("lastKana", "")
				.param("firstKana", "")
				.param("password", "")
				.param("gender", "")
				.param("deptNo", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "password", "gender", "deptNo"));

	}

	// ---------- POST /inputConfirm（その他バリデーションNG） ----------
	@Test
	@DisplayName("登録確認(異常系➁)：その他バリデーションNGで input に戻る（部門リスト存在）")
	void testConfirmRegistration_WhenValidationFails2_ReturnsInput() throws Exception {

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "氏".repeat(11))// 11文字で違反
				.param("firstName", "名".repeat(11))// 11文字で違反
				.param("lastKana", "し".repeat(11))// 11文字で違反
				.param("firstKana", "め".repeat(11))// 11文字で違反
				.param("password", "p".repeat(7)) // 7文字で違反
				.param("gender", "1") //正常
				.param("deptNo", "100")) //正常
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "password"));

	}

	// ---------- POST /saveEmployee ----------
	@Test
	@DisplayName("登録実行：保存して input_complete を返す（ModelMapper不使用）")
	void testSaveEmployee_SavesAndReturnsComplete() throws Exception {
		Employee savedEmployee = new Employee(20001, "山田", "太郎", "ヤマダ", "タロウ", "passwords", 1,
				deptList.get(0));
		Mockito.when(employeeService.save(Mockito.any())).thenReturn(savedEmployee);

		mockMvc.perform(post("/saveEmployee")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input_complete"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(20001))));

	}

	// ---------- GET /deleteConfirm/{empNo} ----------
	@Test
	@DisplayName("削除確認：社員情報と部門情報を表示")
	void testDeleteConfirm_ReturnsEmployeeInfo() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		mockMvc.perform(get("/deleteConfirm/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_confirm"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	// ---------- GET /deleteConfirm/{empNo}（該当なし） ----------
	@Test
	@DisplayName("削除確認（該当なし）：削除できない旨を表示")
	void testDeleteConfirm_NotFound_ReturnsSearchResult() throws Exception {
		Mockito.when(employeeService.findById(99999)).thenReturn(null);

		mockMvc.perform(get("/deleteConfirm/99999"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees", "message"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}

	// ---------- POST /deleteEmployee ----------
	@Test
	@DisplayName("削除実行：削除して delete_complete を返す")
	void testDeleteEmployee_PerformsDeleteAndReturnsComplete() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		mockMvc.perform(post("/deleteEmployee")
				.param("empNo", "10001")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_complete"));

	}

	// ---------- GET /changeInput/{empNo} ----------
	@Test
	@DisplayName("変更画面表示：フォーム値と部門リストをモデルに詰める")
	void testChangeInput_ReturnsFormAndDepartments() throws Exception {

		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/changeInput/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("employeeForm", "departments"))
				.andExpect(model().attribute("employeeForm", hasProperty("empNo", is(10001))))
				.andExpect(model().attribute("departments", hasSize(3)));

	}

	// ---------- POST /changeConfirm（バリデーションOK） ----------
	@Test
	@DisplayName("変更確認(正常系)：バリデーションOKで change_confirm を返す（選択部門をモデルへ）")
	void testChangeConfirm_WhenValid_ReturnsConfirm() throws Exception {
		Mockito.when(departmentService.findById(100)).thenReturn(deptList.get(0));

		mockMvc.perform(post("/changeConfirm")
				.param("empNo", "10001")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change_confirm"))
				.andExpect(model().attributeExists("department"))
				.andExpect(model().attribute("department", hasProperty("deptName",
						is("人事部"))));
	}

	// ---------- POST /changeConfirm（バリデーションNG） ----------
	@Test
	@DisplayName("変更確認(異常系➀)：NotNull&NotBlankバリデーションNGで change に戻る（部門リスト存在）")
	void testChangeConfirm_WhenValidationFails1_ReturnsChange() throws Exception {

		mockMvc.perform(post("/changeConfirm")
				.param("lastName", "")
				.param("firstName", "")
				.param("lastKana", "")
				.param("firstKana", "")
				.param("password", "")
				.param("gender", "")
				.param("deptNo", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "password", "gender", "deptNo"));
	}

	// ---------- POST /changeConfirm（バリデーションNG） ----------
	@Test
	@DisplayName("変更確認(異常系➁)：その他バリデーションNGで change に戻る（部門リスト存在）")
	void testChangeConfirm_WhenValidationFails2_ReturnsChange() throws Exception {

		mockMvc.perform(post("/changeConfirm")
				.param("lastName", "氏".repeat(11))// 11文字で違反
				.param("firstName", "名".repeat(11))// 11文字で違反
				.param("lastKana", "し".repeat(11))// 11文字で違反
				.param("firstKana", "め".repeat(11))// 11文字で違反
				.param("password", "p".repeat(7)) // 7文字で違反
				.param("gender", "1") //正常
				.param("deptNo", "100")) //正常
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "password"));
	}

	// ---------- POST /changeEmployee ----------
	@Test
	@DisplayName("変更実行：保存して change_complete を返す（ModelMapper不使用）")
	void testChangeEmployee_UpdatesAndReturnsComplete() throws Exception {
		Mockito.when(employeeService.update(Mockito.any())).thenReturn(empList.get(0));

		mockMvc.perform(post("/changeEmployee")
				.param("empNo", "10001")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change_complete"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));

	}
}
