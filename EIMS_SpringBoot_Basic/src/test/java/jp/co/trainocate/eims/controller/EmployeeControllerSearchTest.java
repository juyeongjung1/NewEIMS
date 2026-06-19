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
class EmployeeControllerSearchTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	@Test
	@DisplayName("氏名キーワードで社員を検索する")
	void testSelectByEmpName() throws Exception {
		Department dept = new Department(100, "HR");
		List<Employee> employees = List.of(
				new Employee(10001, "Yamada", "Taro", "Yamada", "Taro", "password", 1, dept),
				new Employee(10002, "Yamada", "Hanako", "Yamada", "Hanako", "password", 2, dept));
		when(employeeService.findByEmpName("Yamada")).thenReturn(employees);

		mockMvc.perform(get("/selectByEmpName").param("keyword", "Yamada"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)))
				.andExpect(model().attribute("employees", hasItem(
						hasProperty("department", hasProperty("deptName", is("HR"))))));
	}

	@Test
	@DisplayName("氏名検索で該当なしの場合は空の検索結果を表示する")
	void testSelectByEmpNameEmpty() throws Exception {
		when(employeeService.findByEmpName("Unknown")).thenReturn(List.of());

		mockMvc.perform(get("/selectByEmpName").param("keyword", "Unknown"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}

	@Test
	@DisplayName("社員番号で社員詳細を表示する")
	void testSelectByEmpNo() throws Exception {
		Department dept = new Department(100, "HR");
		Employee employee = new Employee(10001, "Yamada", "Taro", "Yamada", "Taro", "password", 1, dept);
		when(employeeService.findById(10001)).thenReturn(employee);

		mockMvc.perform(get("/selectByEmpNo").param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_detail"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))))
				.andExpect(model().attribute("employee", hasProperty("department", hasProperty("deptName", is("HR")))));
	}

	@Test
	@DisplayName("社員番号検索で該当なしの場合は空の検索結果を表示する")
	void testSelectByEmpNoNoData() throws Exception {
		when(employeeService.findById(99999)).thenReturn(null);

		mockMvc.perform(get("/selectByEmpNo").param("empNo", "99999"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}

	@Test
	@DisplayName("検索条件なしの場合は検索画面に戻る")
	void testSelectByEmpNoNoCondition() throws Exception {
		when(departmentService.findAll()).thenReturn(departments());

		mockMvc.perform(get("/selectByEmpNo"))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	@Test
	@DisplayName("部署番号で社員を検索する")
	void testSelectByDeptNo() throws Exception {
		Department dept = new Department(100, "HR");
		List<Employee> employees = List.of(
				new Employee(10001, "Yamada", "Taro", "Yamada", "Taro", "password", 1, dept));
		when(employeeService.findByDeptNo(100)).thenReturn(employees);

		mockMvc.perform(get("/selectByDeptNo").param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(1)));
	}

	private List<Department> departments() {
		return List.of(
				new Department(100, "HR"),
				new Department(200, "Accounting"),
				new Department(300, "Sales"));
	}
}
