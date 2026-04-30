package jp.co.trainocate.eims.repository;

/**
 * 従業員エンティティ用のリポジトリ。
 */

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.trainocate.eims.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    /**
     * 社員番号を指定して 1 件取得します。
     * @param empNo 検索する社員番号
     * @return 該当する社員リスト
     */
    List<Employee> findByEmpNo(Integer empNo);

    /**
     * 氏(lastName) または 名(firstName) による部分一致検索を行います。
     * @param lastName 検索キーワード（氏）
     * @param firstName 検索キーワード（名）
     * @return 該当する社員リスト
     */
    List<Employee> findByLastNameContainingOrFirstNameContaining(String lastName, String firstName);

    /**
     * 部署番号を基に従業員を検索します。
     * @param deptNo 部署番号
     * @return 従業員リスト
     */
    List<Employee> findByDeptNo(Integer deptNo);
}
