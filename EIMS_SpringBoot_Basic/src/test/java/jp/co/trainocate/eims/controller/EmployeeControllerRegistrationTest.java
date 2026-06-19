package jp.co.trainocate.eims.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
class EmployeeControllerRegistrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	@Test
	@DisplayName("登録画面を表示する")
	void testShowInputPage() throws Exception {
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(get("/input"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	@Test
	@DisplayName("登録確認画面を表示する")
	void testInputConfirm() throws Exception {
		when(departmentService.findById(100)).thenReturn(new Department(100, "HR"));

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "Yamada")
				.param("firstName", "Taro")
				.param("lastKana", "Yamada")
				.param("firstKana", "Taro")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input_confirm"))
				.andExpect(model().attributeExists("department"))
				.andExpect(model().attribute("department", hasProperty("deptName", is("HR"))));
	}

	@Test
	@DisplayName("登録確認で必須エラーの場合は登録画面に戻る")
	void testInputConfirmRequiredError() throws Exception {
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "")
				.param("firstName", "")
				.param("lastKana", "")
				.param("firstKana", "")
				.param("password", "")
				.param("gender", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password", "gender", "deptNo"));
	}

	@Test
	@DisplayName("登録確認で文字数エラーの場合は登録画面に戻る")
	void testInputConfirmSizeError() throws Exception {
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "abcdefghijk")
				.param("firstName", "abcdefghijk")
				.param("lastKana", "abcdefghijklmnopqrstu")
				.param("firstKana", "abcdefghijklmnopqrstu")
				.param("password", "abc")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password"));
	}

	@Test
	@DisplayName("社員を登録する")
	void testSaveEmployee() throws Exception {
		Employee employee = new Employee();
		employee.setEmpNo(20001);
		when(employeeService.save(org.mockito.ArgumentMatchers.any(EmployeeForm.class))).thenReturn(employee);

		mockMvc.perform(post("/saveEmployee")
				.param("lastName", "Yamada")
				.param("firstName", "Taro")
				.param("lastKana", "Yamada")
				.param("firstKana", "Taro")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input_complete"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(20001))));
	}

	private List<Department> departments() {
		return List.of(
				new Department(100, "HR"),
				new Department(200, "Accounting"),
				new Department(300, "Sales"));
	}
}
