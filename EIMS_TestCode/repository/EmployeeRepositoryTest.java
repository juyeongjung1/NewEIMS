package jp.co.trainocate.enshu.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import jp.co.trainocate.enshu.entity.Department;
import jp.co.trainocate.enshu.entity.Employee;
/**
 * EmployeeRepository の振る舞いを検証する JPA 用テストクラス。
 * - プロファイル test の設定（application-test.properties）で MySQL に接続。
 * - 各テストはトランザクションで実行され、終了時にロールバックされる（@DataJpaTest の既定）。
 */
@DataJpaTest // Repository/JPA の最小構成だけ起動し、DB操作をロールバック付きでテストする
@AutoConfigureTestDatabase(replace = Replace.NONE) // 組み込みDBへ差し替えず、プロパティの実DB設定をそのまま使う
class EmployeeRepositoryTest {

	@Autowired
	private EmployeeRepository empRepo;

	@Autowired
	private DepartmentRepository deptRepo;
	
	@Autowired 
	private EntityManager entityManager; // ネイティブSQL実行（AUTO_INCREMENT リセット等）に使用
	
	// 各テストで参照するための共有フィールド
	Employee emp1;
	Employee emp2;
	Employee emp3;
	
	/**
	 * 各テスト前に、テーブル初期化 → 部署マスタ投入 → 社員データ投入、の順で準備する。
	 */
	@BeforeEach
	void setUp() {
	    // 1) 既存データを削除（依存関係の都合で子→親の順で削除）
	    empRepo.deleteAll();
	    deptRepo.deleteAll();
	    
	    // 2) 主キーの採番を 1 からに戻す（MySQL は DDL が暗黙コミットされる点に注意）
	    entityManager.createNativeQuery("ALTER TABLE employee AUTO_INCREMENT = 1").executeUpdate();

	    // 3) 部署マスタを登録（手動採番）
	    Department jinji  = deptRepo.save(new Department(110, "人事部"));
	    Department keiri  = deptRepo.save(new Department(120, "経理部"));

	    // 4) 社員エンティティを作成・保存
	    // 本エンティティは、@ManyToOne Department と 整数FK deptno の二重マッピングを持つ想定。
	    // → department をセットしたら、整合のため deptno も同期させておく。
	    emp1 = new Employee();
	    emp1.setLname("山田"); emp1.setFname("陽翔");
	    emp1.setLkana("ヤマダ"); emp1.setFkana("ヒナタ");
	    emp1.setPassword("password"); emp1.setGender(1);
	    emp1.setDepartment(jinji);
	    emp1.setDeptno(jinji.getDeptno());
	    emp1 = empRepo.save(emp1); // save の戻り値はIDが確定した最新状態

	    emp2 = new Employee();
	    emp2.setLname("中田"); emp2.setFname("結衣");
	    emp2.setLkana("ナカタ"); emp2.setFkana("ユイ");
	    emp2.setPassword("password"); emp2.setGender(2);
	    emp2.setDepartment(keiri);
	    emp2.setDeptno(keiri.getDeptno());
	    emp2 = empRepo.save(emp2);

	    emp3 = new Employee();
	    emp3.setLname("鈴木"); emp3.setFname("大翔");
	    emp3.setLkana("スズキ"); emp3.setFkana("ヒロト");
	    emp3.setPassword("password"); emp3.setGender(1);
	    emp3.setDepartment(jinji);
	    emp3.setDeptno(jinji.getDeptno());
	    emp3 = empRepo.save(emp3);
	}

	/**
	 * 社員番号（主キー）での検索が 1 件ヒットすることを検証。
	 */
	@Test
	void testFindByEmpno() {
		// 1) 実行
		List<Employee> result = empRepo.findByempno(emp1.getEmpno());
		// 2) 件数検証
		assertThat(result).hasSize(1);
		// 3) 内容検証
		assertThat(result.get(0).getLname()).isEqualTo("山田");
	}
	
	/**
	 * 氏名の部分一致検索（姓/名/カナ）で「田」を含む社員が 2 名ヒットすることを検証。
	 * Repository 側は JPQL + LIKEで実装している前提。
	 */
	@Test
	void testFindByEmpNameLike() {
		List<Employee> result = empRepo.findByEmpNameLike("田");
		assertThat(result).hasSize(2);
		// 取得順は未保証のため、順序非依存で比較
		assertThat(result).extracting(Employee::getLname).containsExactlyInAnyOrder("山田","中田");
	}
	
	/**
	 * 部署番号での検索が 2 件ヒットすることを検証。
	 * ※ 取得順はDB/実装に依存して未保証。実務では順序前提の検証は避けるのが無難。
	 */
	@Test
	void testFindByDepartmentDeptno() {
		List<Employee> result = empRepo.findByDepartmentDeptno(110);
		assertThat(result).hasSize(2);
		// ここでは例として先頭要素を簡易チェック（厳密さが必要なら順序を指定して検索する/順序非依存の検証にする）
		assertThat(result.get(0).getLname()).isEqualTo("山田");
		assertThat(result.get(0).getDeptno()).isEqualTo(110);
	}
}