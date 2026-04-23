package jp.co.trainocate.eims.service;

/**
 * 従業員に関するビジネスロジックを定義するサービスインターフェース。
 */

import java.util.List;

import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;

public interface EmployeeService {
    /**
     * 社員情報を全件取得します。
     * @return 全社員リスト
     */
    List<Employee> findAll();

    /**
     * 社員番号を指定して 1 件取得します。
     * @param empno 社員番号
     * @return 該当する社員リスト
     */
    List<Employee> findByEmpno(Integer empno);

    /**
     * 氏名（氏または名）による部分一致検索を行います。
     * @param keyword 検索キーワード
     * @return 該当する社員リスト
     */
    List<Employee> findByEmpName(String keyword);

    /**
     * 部署番号を基に従業員を検索します。
     * @param deptno 部署番号
     * @return 社員のリスト
     */
    List<Employee> findByDeptno(Integer deptno);

    /**
     * 社員番号を指定して 1 件取得します（詳細画面用）。
     * @param empno 社員番号
     * @return 従業員エンティティ
     */
    Employee findById(Integer empno);

    /**
     * 社員情報を保存します。
     * @param employeeForm 入力フォーム
     * @return 保存後のエンティティ
     */
    Employee save(EmployeeForm employeeForm);

    /**
     * 社員番号を指定して 1 件削除します。
     * @param empno 社員番号
     */
    void deleteById(Integer empno);

}
