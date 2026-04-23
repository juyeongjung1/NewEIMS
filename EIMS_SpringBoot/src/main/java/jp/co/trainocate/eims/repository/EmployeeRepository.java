package jp.co.trainocate.eims.repository;

/**
 * 従業員エンティティ用のリポジトリ。
 */

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.co.trainocate.eims.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
        /**
         * 主キーである社員番号を条件に 1 件取得します。
         * @param empno 検索する社員番号
         * @return 該当する社員リスト
         */
        List<Employee> findByempno(int empno);

        // JPQL を利用した氏名の部分一致検索
        @Query("SELECT e " +
                        "FROM Employee e " +
                        "WHERE e.lname LIKE %:keyword% " +
                        "   OR e.fname LIKE %:keyword% " +
                        "   OR e.lkana LIKE %:keyword% " +
                        "   OR e.fkana LIKE %:keyword%")
        List<Employee> findByEmpNameLike(String keyword);
	
	/*	//JPQLを使わないパターン
		List<Employee> findByLnameLikeOrFnameLikeOrLkanaLikeOrFkanaLike(String lname, String fname, String lkana,
				String fkana);*/

    /**
     * 部署番号を基に従業員を検索します。
     * @param deptno 部署番号
     * @return 従業員リスト
     */
    List<Employee> findByDepartmentDeptno(Integer deptno);
}
