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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
// テスト用のapplication-test.propertiesを適用し、本番環境に影響を与えないようにします
@ActiveProfiles("test")
class EmployeeControllerTest {

	// 疑似的にGETやPOSTリクエストを送信し、Controllerの動作をシミュレーションするための機能です
	@Autowired
	private MockMvc mockMvc;

	// Serviceをモック化（偽物に）して、RepositoryやDBの影響を受けずにControllerの動作だけに集中します
	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private DepartmentService departmentService;

	private List<Department> deptList;
	private List<Employee> empList;

	@BeforeEach
	void setUp() {
		// ① テスト用のモックデータ（偽データ）を定義します。
		deptList = List.of(
				new Department(100, "人事部", null),
				new Department(200, "経理部", null),
				new Department(300, "営業部", null));

		empList = List.of(
				new Employee(10001, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, deptList.get(0).getDeptNo(), deptList.get(0)),
				new Employee(10002, "中田", "結衣", "ナカタ", "ユイ", "password", 2, deptList.get(0).getDeptNo(), deptList.get(0)),
				new Employee(10003, "鈴木", "大翔", "スズキ", "ヒロト", "password", 1, deptList.get(2).getDeptNo(), deptList.get(2)));
	}

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
		// ② 偽物のServiceが呼ばれたら、用意したモックデータを返すように設定（スタブ設定）します。
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		// ③ 疑似的にGETリクエストを送信し、結果を検証します。
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
		// ① モックデータの準備
		List<Employee> mockEmployees = List.of(
				empList.get(0), // 山田
				empList.get(1)  // 中田
		);
		// ② スタブ設定
		Mockito.when(employeeService.findByEmpName("田")).thenReturn(mockEmployees);

		// ③ リクエスト送信と検証
		mockMvc.perform(get("/selectByEmpName").param("keyword", "田"))
				.andExpect(status().isOk()) // 通信ステータスが200 OK（成功）であること
				.andExpect(view().name("search_result")) // 遷移先のHTML（ビュー名）が正しいこと
				.andExpect(model().attributeExists("employees")) // Modelに"employees"というキーが存在すること
				.andExpect(model().attribute("employees", hasSize(2))) // Modelに格納されたリストの要素数が2件であること
				// 部署情報も正しくModelのデータに紐づいて格納されているか検証します
				.andExpect(model().attribute("employees", hasItem(
						hasProperty("department", hasProperty("deptName", is("人事部")))
				)));
	}

	@Test
	@DisplayName("氏名キーワード検索APIのテスト_異常系(データ0件)")
	void testSelectByEmpName_empty() throws Exception {
		// 異常系: DBにデータが1件もない（空のリストが返る）状況をシミュレーションします
		Mockito.when(employeeService.findByEmpName("存在しない")).thenReturn(List.of());

		mockMvc.perform(get("/selectByEmpName").param("keyword", "存在しない"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				.andExpect(model().attribute("employees", hasSize(0)))
				// Thymeleafでレンダリングされた後のHTMLテキストに、特定のメッセージが含まれているかを検証します
				.andExpect(content().string(containsString("検索条件に一致する社員は見つかりませんでした。")));
	}

	@Test
	@DisplayName("社員番号を指定して取得するAPIのテスト_正常系")
	void testSelectByEmpNo_normal() throws Exception {
		// ① 引数に「10001」が渡されたときに、モックデータを返すよう設定します。
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		// ② URLパラメータ（?empNo=10001）を付与してGETリクエストを送信します。
		mockMvc.perform(get("/selectByEmpNo").param("empNo", "10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee_detail"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("lastName", is("山田")))) // 取得した商品のlastNameが"山田"か確認
				.andExpect(model().attribute("employee", hasProperty("department", hasProperty("deptName", is("人事部")))));
	}

	@Test
	@DisplayName("社員番号を指定して取得するAPIのテスト_異常系(該当データなし)")
	void testSelectByEmpNo_noData() throws Exception {
		// 異常系: 存在しないID（99999）が指定され、データが見つからない（nullが返る）状況をシミュレーションします。
		Mockito.when(employeeService.findById(99999)).thenReturn(null);

		mockMvc.perform(get("/selectByEmpNo").param("empNo", "99999"))
				.andExpect(status().isOk())
				.andExpect(view().name("search_result"))
				// データが存在しないため、空のリストが返され、該当メッセージが表示されることを検証します。
				.andExpect(model().attribute("employees", hasSize(0)))
				.andExpect(content().string(containsString("検索条件に一致する社員は見つかりませんでした。")));
	}

	// ==========================================
	// 2. 登録機能のテスト
	// ==========================================

	@Test
	@DisplayName("登録画面への遷移テスト")
	void testShowInputPage_normal() throws Exception {
		// ① 登録画面のセレクトボックス（プルダウン）用に部署リストのモックを用意します
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/input"))
				.andExpect(status().isOk())
				.andExpect(view().name("input"))
				.andExpect(model().attributeExists("departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}

	@Test
	@DisplayName("登録確認APIのテスト_正常系")
	void testConfirmRegistration_normal() throws Exception {
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
	@DisplayName("登録確認APIのテスト_異常系(バリデーションエラー)")
	void testConfirmRegistration_validationError() throws Exception {
		// 必須項目を空にしてバリデーションエラーを発生させる状況をシミュレーションします
		mockMvc.perform(post("/inputConfirm")
				.param("lastName", "")
				.param("firstName", "")
				.param("lastKana", "")
				.param("firstKana", "")
				.param("password", "")
				.param("gender", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("input")) // エラー時は入力画面に戻ること
				.andExpect(model().attributeExists("departments"))
				// EmployeeFormの各フィールドにエラーが格納されていることを検証します
				.andExpect(model().attributeHasFieldErrors(
						"employeeForm", "lastName", "firstName", "lastKana", "firstKana", "password", "gender"));
	}

	@Test
	@DisplayName("登録実行APIのテスト_正常系")
	void testSaveEmployee_normal() throws Exception {
		when(departmentService.findById(100)).thenReturn(deptList.get(0));

		// saveメソッドが呼ばれたら、採番されたモックEmployeeを返すよう設定します
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
	
	// ==========================================
	// 3. 更新・削除機能のテスト
	// ==========================================

	@Test
	@DisplayName("削除確認画面の表示テスト_正常系")
	void testDeleteConfirm_normal() throws Exception {
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));

		mockMvc.perform(get("/deleteConfirm/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("delete_confirm"))
				.andExpect(model().attributeExists("employee"))
				.andExpect(model().attribute("employee", hasProperty("empNo", is(10001))));
	}

	@Test
	@DisplayName("変更入力画面の表示テスト_正常系")
	void testChangeInput_normal() throws Exception {
		// 初期表示のために、DBから既存データを取得するシミュレーション
		Mockito.when(employeeService.findById(10001)).thenReturn(empList.get(0));
		Mockito.when(departmentService.findAll()).thenReturn(deptList);

		mockMvc.perform(get("/changeInput/10001"))
				.andExpect(status().isOk())
				.andExpect(view().name("change"))
				.andExpect(model().attributeExists("employeeForm", "departments"))
				.andExpect(model().attribute("departments", hasSize(3)));
	}
}
