package jp.co.trainocate.eims.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	// ==========================================
	// 0. トップ・一覧・検索画面表示のテスト
	// ==========================================

	@Test
	@DisplayName("index画面表示のテスト_正常系")
	void testIndex_normal() throws Exception {
		mockMvc.perform(get("/index"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	@Test
	@DisplayName("検索画面の表示テスト_正常系")
	void testShowSearchPage_normal() throws Exception {
		List<Department> mockDepartments = List.of(
				new Department(100, "人事部"),
				new Department(200, "経理部"),
				new Department(300, "営業部")
		);
		Mockito.when(departmentService.findAll()).thenReturn(mockDepartments);

		mockMvc.perform(get("/search"))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	// ==========================================
	// 1. 検索機能のテスト
	// ==========================================

	@Test
	@DisplayName("氏名キーワード検索APIのテスト_正常系")
	void testSelectByEmpName_normal() throws Exception {
		Department mockDept = new Department(100, "人事部");
		List<Employee> mockEmployees = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, mockDept, 0, 0),
				new Employee(10002, "中田", "結衣", "ナカタ", "ユイ", "password", 2, mockDept, 0, 0)
		);
		Mockito.when(employeeService.findByEmpName("田")).thenReturn(mockEmployees);

		mockMvc.perform(get("/selectByEmpName").param("keyword", "田"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)))
				.andExpect(model().attribute("employees", hasItem(
						hasProperty("department", hasProperty("deptName", is("人事部")))
				)));
	}

	@Test
	@DisplayName("社員番号を指定して取得するAPIのテスト_正常系")
	void testSelectByEmpNo_normal() throws Exception {
		Department mockDept = new Department(100, "人事部");
		Employee mockEmployee = new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, mockDept, 0, 0);
		// コントローラーの変更に合わせて List を返すように設定
		Mockito.when(employeeService.findByEmpNo(10001)).thenReturn(List.of(mockEmployee));

		mockMvc.perform(get("/selectByEmpNo").param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_detail"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("lastName", is("山田"))));
	}

	// ==========================================
	// 2. 登録機能のテスト
	// ==========================================

	@Test
	@DisplayName("登録確認APIのテスト_正常系")
	void testConfirmRegistration_normal() throws Exception {
		Department mockDept = new Department(100, "人事部");
		Mockito.when(departmentService.findById(100)).thenReturn(mockDept);

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords") // 8文字以上
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input_confirm"))
				.andExpect(model().attributeExists("department"));
	}

	@Test
	@DisplayName("登録確認APIのテスト_異常系(文字数制限違反)")
	void testConfirmRegistration_error_size() throws Exception {
		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "あいうえおかきくけこさしすせそ") // 10文字超
				.param("firstName", "あいうえおかきくけこさしすせそ") // 10文字超
				.param("lastKana", "アイウエオカキクケコサシスセソタチツテトナニヌネノ") // 20文字超
				.param("firstKana", "アイウエオカキクケコサシスセソタチツテトナニヌネノ") // 20文字超
				.param("password", "short") // 8文字未満
				.param("gender", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password"));
	}

	@Test
	@DisplayName("登録実行APIのテスト_正常系")
	void testSaveEmployee_normal() throws Exception {
		Employee mockSavedEmployee = new Employee();
		mockSavedEmployee.setEmpNo(20001);
		Mockito.when(employeeService.save(any(EmployeeForm.class))).thenReturn(mockSavedEmployee);

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
				.andExpect(model().attributeExists("employee"));
	}
	
	// ==========================================
	// 3. 更新・削除機能のテスト
	// ==========================================

	@Test
	@DisplayName("削除確認画面の表示テスト_正常系")
	void testDeleteConfirm_normal() throws Exception {
		Department mockDept = new Department(100, "人事部");
		Employee mockEmployee = new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, mockDept, 0, 0);
		Mockito.when(employeeService.findById(10001)).thenReturn(mockEmployee);

		mockMvc.perform(get("/deleteConfirm/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_confirm"))
				.andExpect(model().attributeExists("employee"));
	}

	@Test
	@DisplayName("変更入力画面の表示テスト_正常系")
	void testChangeInput_normal() throws Exception {
		Department mockDept = new Department(100, "人事部");
		Employee mockEmployee = new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, mockDept, 0, 0);
		List<Department> mockDepartments = List.of(new Department(100, "人事部"));
		
		Mockito.when(employeeService.findById(10001)).thenReturn(mockEmployee);
		Mockito.when(departmentService.findAll()).thenReturn(mockDepartments);

		mockMvc.perform(get("/changeInput/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("employeeForm", "departments"));
	}
}
