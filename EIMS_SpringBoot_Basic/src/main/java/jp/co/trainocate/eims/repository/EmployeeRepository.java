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
     * 社員番号を指定して 1 件取得します。
     * @param empno 検索する社員番号
     * @return 該当する社員リスト
     */
    List<Employee> findByEmpno(Integer empno);

    /**
     * 氏(lname) または 名(fname) による部分一致検索を行います。
     * @param lname 検索キーワード（氏）
     * @param fname 検索キーワード（名）
     * @return 該当する社員リスト
     */
    List<Employee> findByLnameContainingOrFnameContaining(String lname, String fname);

    /**
     * 部署番号を基に従業員を検索します。
     * @param deptno 部署番号
     * @return 従業員リスト
     */
    List<Employee> findByDeptno(Integer deptno);
}

