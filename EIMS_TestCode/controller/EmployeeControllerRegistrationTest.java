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
class EmployeeControllerRegistrationTest {

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
				.andExpect(model().attribute("department", hasProperty("deptName", is("人事部"))));
	}

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
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password", "gender", "deptNo"));
	}

	@Test
	@DisplayName("登録確認(異常系➁)：その他バリデーションNGで input に戻る（部門リスト存在）")
	void testConfirmRegistration_WhenValidationFails2_ReturnsInput() throws Exception {
		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "氏".repeat(11))
				.param("firstName", "名".repeat(11))
				.param("lastKana", "し".repeat(21))
				.param("firstKana", "め".repeat(21))
				.param("password", "p".repeat(17))
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password"));
	}

	@Test
	@DisplayName("登録実行：保存して input_complete を返す（ModelMapper不使用）")
	void testSaveEmployee_SavesAndReturnsComplete() throws Exception {
		Employee savedEmployee = new Employee(20001, "山田", "太郎", "ヤマダ", "タロウ", "passwords", 1,
				deptList.get(0));
		Mockito.when(employeeService.save(Mockito.any())).thenReturn(savedEmployee);

		// 本テストは保存結果のモデル格納と画面遷移（input_complete）までを検証する。
		// フォーム値がサービスへ正しく渡ったか（引数検証）は Mockito.verify/ArgumentCaptor を要し、本演習の範囲外。
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
	@DisplayName("登録確認から「修正する」（POST /input）：入力値を保持して input に戻る")
	void testBackToInputPage_ReturnsInputWithDepartments() throws Exception {
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(post("/input")
				.param("lastName", "山田")
				.param("firstName", "太郎")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "タロウ")
				.param("password", "passwords")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)))
				// 「修正する」で戻る際、入力値が保持されている（employeeForm に反映）
				.andExpect(model().attribute("employeeForm", hasProperty("lastName", is("山田"))));
	}
}
