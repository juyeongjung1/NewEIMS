package jp.co.trainocate.eims.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;

/**
 * EmployeeRepository の振る舞いを検証する JPA 用テストクラス。
 * - プロファイル test の設定（application-test.properties）で MySQL に接続。
 * - 各テストはトランザクションで実行され、終了時にロールバックされる（@DataJpaTest の既定）。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class EmployeeRepositoryTest {

	@Autowired
	private EmployeeRepository empRepo;

	@Autowired
	private DepartmentRepository deptRepo;
	
	@Autowired 
	private EntityManager entityManager;
	
	Employee emp1;
	Employee emp2;
	Employee emp3;
	
	@BeforeEach
	void setUp() {
	    // 既存データを削除
	    empRepo.deleteAll();
	    deptRepo.deleteAll();
	    
	    // 主キーの採番をリセット
	    entityManager.createNativeQuery("ALTER TABLE employee AUTO_INCREMENT = 1").executeUpdate();

	    // 部署マスタを登録
	    Department jinji  = deptRepo.save(new Department(110, "人事部"));
	    Department keiri  = deptRepo.save(new Department(120, "経理部"));

	    // 社員データ登録
	    // ★ Repositoryテストでは実DBにINSERTするため、deptNoをsetterで別途セットする
	    emp1 = new Employee();
	    emp1.setLastName("山田"); emp1.setFirstName("陽翔");
	    emp1.setLastKana("ヤマダ"); emp1.setFirstKana("ヒナタ");
	    emp1.setPassword("password"); emp1.setGender(1);
	    emp1.setDepartment(jinji);
	    emp1.setDeptNo(jinji.getDeptNo());
	    emp1 = empRepo.save(emp1);

	    emp2 = new Employee();
	    emp2.setLastName("中田"); emp2.setFirstName("結衣");
	    emp2.setLastKana("ナカタ"); emp2.setFirstKana("ユイ");
	    emp2.setPassword("password"); emp2.setGender(2);
	    emp2.setDepartment(keiri);
	    emp2.setDeptNo(keiri.getDeptNo());
	    emp2 = empRepo.save(emp2);

	    emp3 = new Employee();
	    emp3.setLastName("鈴木"); emp3.setFirstName("大翔");
	    emp3.setLastKana("スズキ"); emp3.setFirstKana("ヒロト");
	    emp3.setPassword("password"); emp3.setGender(1);
	    emp3.setDepartment(jinji);
	    emp3.setDeptNo(jinji.getDeptNo());
	    emp3 = empRepo.save(emp3);
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
