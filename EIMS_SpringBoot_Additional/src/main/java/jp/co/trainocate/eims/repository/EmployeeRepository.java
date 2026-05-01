package jp.co.trainocate.eims.repository;

/**
 * 従業員エンティティ用のリポジトリ。
 */

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.trainocate.eims.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    /**
     * 社員番号と削除フラグを指定して取得します。
     * @param empNo 社員番号
     * @param deleteFlg 削除フラグ
     * @return 該当する社員リスト
     */
    /**
     * 氏名（氏または名）の部分一致検索かつ削除フラグ指定で取得します。
     * @param lastName 検索キーワード（氏）
     * @param firstName 検索キーワード（名）
     * @param deleteFlg 削除フラグ
     * @return 該当する社員リスト
     */
    List<Employee> findByLastNameContainingOrFirstNameContaining(String lastName, String firstName);

    /**
     * 部署番号と削除フラグを指定して検索します。
     * @param deptNo 部署番号
     * @param deleteFlg 削除フラグ
     * @return 従業員リスト
     */
    List<Employee> findByDeptNo(Integer deptNo);

    /**
     * 削除フラグを指定して全件取得します（退職者一覧用など）。
     * @param deleteFlg 削除フラグ
     * @return 従業員リスト
     */
    List<Employee> findByDeleteFlg(Integer deleteFlg);
}
