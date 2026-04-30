package jp.co.trainocate.eims.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	private List<Department> deptList;
	private List<Employee> empList;

	@BeforeEach
	void setUp() {
		deptList = List.of(
				new Department(100, "人事部", null),
				new Department(200, "経理部", null),
				new Department(300, "営業部", null));

		empList = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0).getDeptNo(), deptList.get(0)),
				new Employee(10002, "中田", "結衣", "ナカタ", "ユイ", "password", 2, deptList.get(0).getDeptNo(), deptList.get(0)),
				new Employee(10003, "鈴木", "大翔", "スズキ", "ヒロト", "password", 1, deptList.get(2).getDeptNo(), deptList.get(2)));
	}

	@Test
	@DisplayName("index画面の表示：index ビューが返る")
	void testIndex_ReturnsIndexView() throws Exception {
		mockMvc.perform(get("/index"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

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

	@Test
	@DisplayName("社員番号検索（パラメータあり）：結果を employee_detail に表示")
	void testSelectByEmpNo_WithParam_ReturnsDetail() throws Exception {
		Employee mockEmployee = new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0).getDeptNo(), deptList.get(0));
		Mockito.when(employeeService.findById(10001)).thenReturn(mockEmployee);
		
		mockMvc.perform(get("/selectByEmpNo").param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_detail"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("lastName", is("山田"))));
	}

	@Test
	@DisplayName("社員番号検索（パラメータ無し）：検索画面に戻る")
	void testSelectByEmpNo_NoParam_ReturnsSearch() throws Exception {
		mockMvc.perform(get("/selectByEmpNo"))
				.andExpect(status().isOk())
				.andExpect(view().name("search"));
	}

	@Test
	@DisplayName("氏名キーワード検索：「田」の検索結果を search_result に表示")
	void testSelectByEmpName_ReturnsResult() throws Exception {
		List<Employee> mockEmployees = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0).getDeptNo(), deptList.get(0)),
				new Employee(10002, "中田", "結衣", "ナカタ", "ユイ", "password", 2, deptList.get(0).getDeptNo(), deptList.get(0)));
		Mockito.when(employeeService.findByEmpName("田")).thenReturn(mockEmployees);

		mockMvc.perform(get("/selectByEmpName").param("keyword", "田"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)));
	}

	@Test
	@DisplayName("部門番号検索：結果を search_result に表示")
	void testSelectByDeptNo_ReturnsResult() throws Exception {
		List<Employee> mockEmployees = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0).getDeptNo(), deptList.get(0)),
				new Employee(10002, "中田", "結衣", "ナカタ", "ユイ", "password", 2, deptList.get(0).getDeptNo(), deptList.get(0)));
		Mockito.when(employeeService.findByDeptNo(100)).thenReturn(mockEmployees);

		mockMvc.perform(get("/selectByDeptNo").param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)));
	}

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

	@Test
	@DisplayName("登録確認(正常系)：バリデーションOKで input_confirm を返す")
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
				.andExpect(model().attribute("department", hasProperty("deptName", is("人事部"))));
	}

	@Test
	@DisplayName("登録確認(異常系)：バリデーションNGで input に戻る")
	void testConfirmRegistration_WhenValidationFails_ReturnsInput() throws Exception {
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
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password", "gender"));
	}

	@Test
	@DisplayName("登録実行：保存して input_complete を返す")
	void testSaveEmployee_SavesAndReturnsComplete() throws Exception {
		when(departmentService.findById(100)).thenReturn(deptList.get(0));

		when(employeeService.save(any(EmployeeForm.class))).thenAnswer(inv -> {
			EmployeeForm f = inv.getArgument(0);
			Employee e = new Employee();
			e.setEmpNo(20001);
			e.setDeptNo(f.getDeptNo());
			return e;
		});

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
	
	@Test
	@DisplayName("削除確認：社員情報を表示")
	void testDeleteConfirm_ReturnsEmployeeInfo() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		mockMvc.perform(get("/deleteConfirm/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_confirm"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	@Test
	@DisplayName("削除実行：削除して delete_complete を返す")
	void testDeleteEmployee_PerformsDeleteAndReturnsComplete() throws Exception {
		mockMvc.perform(post("/deleteEmployee")
				.param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_complete"));
	}
	
	@Test
	@DisplayName("変更画面表示：DBからデータを取得してフォームにセット")
	void testChangeInput_ReturnsFormAndDepartments() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/changeInput/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("employeeForm", "departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}
}
