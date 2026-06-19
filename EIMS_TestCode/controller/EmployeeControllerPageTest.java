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
import jp.co.trainocate.enshu.service.DepartmentService;
import jp.co.trainocate.enshu.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerPageTest {

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
}
