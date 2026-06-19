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
class EmployeeControllerMaintenanceTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	@Test
	@DisplayName("削除確認画面を表示する")
	void testDeleteConfirm() throws Exception {
		Employee employee = employee(10001);
		when(employeeService.findById(10001)).thenReturn(employee);

		mockMvc.perform(get("/deleteConfirm/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_confirm"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	@Test
	@DisplayName("削除対象がない場合は検索結果画面に戻る")
	void testDeleteConfirmNoData() throws Exception {
		when(employeeService.findById(99999)).thenReturn(null);

		mockMvc.perform(get("/deleteConfirm/99999"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees", "message"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}

	@Test
	@DisplayName("社員を削除する")
	void testDeleteEmployee() throws Exception {
		when(employeeService.findById(10001)).thenReturn(employee(10001));

		mockMvc.perform(post("/deleteEmployee").param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_complete"));

		verify(employeeService).deleteById(10001);
	}

	@Test
	@DisplayName("変更画面を表示する")
	void testChangeInput() throws Exception {
		when(employeeService.findById(10001)).thenReturn(employee(10001));
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(get("/changeInput/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("employeeForm", "departments"))
				.andExpect(model().attribute("employeeForm", hasProperty("empNo", is(10001))))
				.andExpect(model().attribute("departments", hasSize(3)))
				.andExpect(content().string(not(containsString(" required"))))
				.andExpect(content().string(containsString("value=\"\"")));
	}

	@Test
	@DisplayName("変更確認画面を表示する")
	void testChangeConfirm() throws Exception {
		when(departmentService.findById(100)).thenReturn(new Department(100, "HR"));

		mockMvc.perform(post("/changeConfirm")
				.param("empNo", "10001")
				.param("lastName", "Yamada")
				.param("firstName", "Taro")
				.param("lastKana", "Yamada")
				.param("firstKana", "Taro")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change_confirm"))
				.andExpect(model().attributeExists("department"))
				.andExpect(model().attribute("department", hasProperty("deptName", is("HR"))));
	}

	@Test
	@DisplayName("変更確認で必須エラーの場合は変更画面に戻る")
	void testChangeConfirmRequiredError() throws Exception {
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(post("/changeConfirm")
				.param("empNo", "10001")
				.param("lastName", "")
				.param("firstName", "")
				.param("lastKana", "")
				.param("firstKana", "")
				.param("password", "")
				.param("gender", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password", "gender", "deptNo"))
				.andExpect(content().string(containsString("氏は必須項目です")))
				.andExpect(content().string(containsString("名は必須項目です")))
				.andExpect(content().string(containsString("性別は必須項目です")))
				.andExpect(content().string(containsString("部署は必須項目です")));
	}

	@Test
	@DisplayName("社員を変更する")
	void testChangeEmployee() throws Exception {
		Employee employee = new Employee();
		employee.setEmpNo(10001);
		when(employeeService.update(org.mockito.ArgumentMatchers.any(EmployeeForm.class))).thenReturn(employee);

		mockMvc.perform(post("/changeEmployee")
				.param("empNo", "10001")
				.param("lastName", "Yamada")
				.param("firstName", "Taro")
				.param("lastKana", "Yamada")
				.param("firstKana", "Taro")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change_complete"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	private Employee employee(Integer empNo) {
		return new Employee(empNo, "Yamada", "Taro", "Yamada", "Taro", "password", 1, new Department(100, "HR"));
	}

	private List<Department> departments() {
		return List.of(
				new Department(100, "HR"),
				new Department(200, "Accounting"),
				new Department(300, "Sales"));
	}
}
