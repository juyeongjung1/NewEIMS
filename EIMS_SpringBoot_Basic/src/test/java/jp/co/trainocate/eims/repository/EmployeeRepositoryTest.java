package jp.co.trainocate.eims.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;

/**
 * EmployeeRepository の振る舞いを検証する JPA 用テストクラス。
 * - 各テストはトランザクションで実行され、終了時にロールバックされる（@DataJpaTest の既定）。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class EmployeeRepositoryTest {

	@Autowired
	private EmployeeRepository empRepo;

	@Autowired
	private DepartmentRepository deptRepo;

	Employee emp1;
	Employee emp2;
	Employee emp3;

	@BeforeEach
	void setUp() {
		// 既存データを削除してリセット
		empRepo.deleteAll();
		deptRepo.deleteAll();

		// 部署を作成
		Department jinji = deptRepo.save(new Department(110, "人事部"));
		Department keiri = deptRepo.save(new Department(120, "経理部"));

		// 部署を紐づけた Employee を登録
		emp1 = empRepo.save(new Employee(null, "山田", "陽翔", "ヤマダ", "ヒナタ", "password", 1, jinji));
		emp2 = empRepo.save(new Employee(null, "中田", "結衣", "ナカタ", "ユイ", "password", 2, keiri));
		emp3 = empRepo.save(new Employee(null, "鈴木", "大翔", "スズキ", "ヒロト", "password", 1, jinji));
	}

	@Test
	void testFindByEmpNo() {
		List<Employee> result = empRepo.findByEmpNo(emp1.getEmpNo());

		assertThat(result).hasSize(1);
		// メインテキストで推奨されている tuple による検証
		assertThat(result).extracting(Employee::getLastName, Employee::getFirstName)
				.containsExactly(tuple("山田", "陽翔"));
	}

	@Test
	void testFindByLastNameContainingOrFirstNameContaining() {
		// "田" を氏または名に含む社員を検索（山田、中田がヒットする想定）
		List<Employee> result = empRepo.findByLastNameContainingOrFirstNameContaining("田", "田");

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Employee::getLastName, Employee::getFirstName)
				.containsExactlyInAnyOrder(tuple("山田", "陽翔"), tuple("中田", "結衣"));
	}

	@Test
	void testFindByDeptNo() {
		// 人事部(110)に所属する社員を検索
		List<Employee> result = empRepo.findByDeptNo(110);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Employee::getLastName, Employee::getFirstName)
				.containsExactlyInAnyOrder(tuple("山田", "陽翔"), tuple("鈴木", "大翔"));
	}
}
