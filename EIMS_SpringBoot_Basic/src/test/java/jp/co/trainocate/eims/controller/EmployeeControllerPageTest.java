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
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerPageTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	@Test
	@DisplayName("index画面を表示する")
	void testIndex() throws Exception {
		mockMvc.perform(get("/index"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	@Test
	@DisplayName("検索画面を表示する")
	void testShowSearchPage() throws Exception {
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(get("/search"))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	@Test
	@DisplayName("社員一覧画面を表示する")
	void testShowEmployeeList() throws Exception {
		Department dept = new Department(100, "HR");
		List<Employee> employees = List.of(
				new Employee(10001, "Yamada", "Taro", "Yamada", "Taro", "password", 1, dept),
				new Employee(10002, "Sato", "Hanako", "Sato", "Hanako", "password", 2, dept));
		when(employeeService.findAll()).thenReturn(employees);

		mockMvc.perform(get("/employeeList"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_list"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)));
	}

	private List<Department> departments() {
		return List.of(
				new Department(100, "HR"),
				new Department(200, "Accounting"),
				new Department(300, "Sales"));
	}
}
