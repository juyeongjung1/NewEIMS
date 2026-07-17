package jp.co.trainocate.eims.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class EmployeeControllerPageTest {

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

	@Test
	@DisplayName("index画面の表示：index ビューが返る")
	void testIndex_ReturnsIndexView() throws Exception {
		mockMvc.perform(get("/index"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	@Test
	@DisplayName("トップページの表示（/）：index ビューが返る（/index の別名）")
	void testRoot_ReturnsIndexView() throws Exception {
		mockMvc.perform(get("/"))
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
	@DisplayName("社員一覧画面の表示：社員リストをモデルに詰めて employee_list を返す")
	void testShowEmployeeList_ReturnsEmployeeList() throws Exception {
		List<Employee> empList = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0)),
				new Employee(10002, "中田", "結衣", "タナカ", "ユイ", "password", 2, deptList.get(0)));
		Mockito.when(employeeService.findAll()).thenReturn(empList);

		mockMvc.perform(get("/employeeList"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_list"))
				.andExpect(model().attributeExists("employees"))
				.andExpect(model().attribute("employees", hasSize(2)));
	}
}
