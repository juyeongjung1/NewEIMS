package jp.co.trainocate.eims.repository;

/**
 * 従業員エンティティ用のリポジトリ。
 */

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.trainocate.eims.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
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

    /**
     * 氏または名にキーワードを含む在籍社員を検索します。
     * @param deleteFlg1 削除フラグ（氏検索用）
     * @param lastName 検索キーワード（氏）
     * @param deleteFlg2 削除フラグ（名検索用）
     * @param firstName 検索キーワード（名）
     * @return 該当する社員リスト
     */
    List<Employee> findByDeleteFlgAndLastNameContainingOrDeleteFlgAndFirstNameContaining(
            Integer deleteFlg1, String lastName, Integer deleteFlg2, String firstName);

    /**
     * 部署番号と削除フラグを基に従業員を検索します。
     * @param deptNo 部署番号
     * @param deleteFlg 削除フラグ
     * @return 従業員リスト
     */
    List<Employee> findByDeptNoAndDeleteFlg(Integer deptNo, Integer deleteFlg);

    /**
     * 削除フラグを指定して全件取得します（退職者一覧用など）。
     * @param deleteFlg 削除フラグ
     * @return 従業員リスト
     */
    List<Employee> findByDeleteFlg(Integer deleteFlg);
}
