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
class EmployeeControllerMaintenanceTest {

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

	private List<Employee> empList = List.of(
			new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0)),
			new Employee(10002, "中田", "結衣", "タナカ", "ユイ", "password", 2, deptList.get(0)),
			new Employee(10003, "鈴木", "大翔", "スズキ", "ヒロト", "password", 1, deptList.get(2)));

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

	@Test
	@DisplayName("削除実行：削除リクエストで delete_complete を返す")
	void testDeleteEmployee_PerformsDeleteAndReturnsComplete() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		// 本テストは画面遷移（delete_complete）までを検証する。
		// サービス呼び出し（deleteById）の検証には Mockito.verify が必要で、本演習の範囲外。
		mockMvc.perform(post("/deleteEmployee")
				.param("empNo", "10001")
				.param("lastName", "山田")
				.param("firstName", "陽翔")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "ヒナタ")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_complete"));
	}

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
				.andExpect(model().attribute("department", hasProperty("deptName", is("人事部"))));
	}

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
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password", "gender", "deptNo"));
	}

	@Test
	@DisplayName("変更確認(異常系➁)：その他バリデーションNGで change に戻る（部門リスト存在）")
	void testChangeConfirm_WhenValidationFails2_ReturnsChange() throws Exception {
		mockMvc.perform(post("/changeConfirm")
				.param("lastName", "氏".repeat(11))
				.param("firstName", "名".repeat(11))
				.param("lastKana", "し".repeat(21))
				.param("firstKana", "め".repeat(21))
				.param("password", "p".repeat(17))
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password"));
	}

	@Test
	@DisplayName("変更実行：保存して change_complete を返す（ModelMapper不使用）")
	void testChangeEmployee_UpdatesAndReturnsComplete() throws Exception {
		Mockito.when(employeeService.update(Mockito.any())).thenReturn(empList.get(0));

		// 本テストは更新結果のモデル格納と画面遷移（change_complete）までを検証する。
		// フォーム値がサービスへ正しく渡ったか（引数検証）は Mockito.verify/ArgumentCaptor を要し、本演習の範囲外。
		mockMvc.perform(post("/changeEmployee")
				.param("empNo", "10001")
				.param("lastName", "山田")
				.param("firstName", "陽翔")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "ヒナタ")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change_complete"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	@Test
	@DisplayName("変更確認から「修正する」（POST /changeInput/{empNo}）：入力値を保持して change に戻る")
	void testBackToChangeInput_ReturnsChangeWithDepartments() throws Exception {
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(post("/changeInput/10001")
				.param("empNo", "10001")
				.param("lastName", "山田")
				.param("firstName", "陽翔")
				.param("lastKana", "ヤマダ")
				.param("firstKana", "ヒナタ")
				.param("password", "password")
				.param("gender", "1")
				.param("deptNo", "100"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)))
				// 「修正する」で戻る際、入力値が保持されている（employeeForm に反映）
				.andExpect(model().attribute("employeeForm", hasProperty("empNo", is(10001))));
	}

	@Test
	@DisplayName("削除実行（対象なし）：削除できない旨を表示")
	void testDeleteEmployee_NotFound_ReturnsSearchResult() throws Exception {
		Mockito.when(employeeService.findById(99999)).thenReturn(null);

		mockMvc.perform(post("/deleteEmployee").param("empNo", "99999"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attributeExists("employees", "message"))
				.andExpect(model().attribute("employees", hasSize(0)));
	}
}
