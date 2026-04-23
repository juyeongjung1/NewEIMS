package jp.co.trainocate.eims.service;

/**
 * 従業員に関するビジネスロジックを定義するサービスインターフェース。
 */

import java.util.List;

import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;

public interface EmployeeService {
    /**
     * 社員番号を指定して 1 件の社員情報を取得します。
     * @param empno 検索したい社員番号
     * @return 条件に一致する社員リスト
     */
    List<Employee> findByEmpNo(int empno);

    /**
     * 氏名の一部をキーワードとしてあいまい検索します。
     * @param keyword 検索キーワード
     * @return 該当する社員リスト
     */
    List<Employee> findByEmpName(String keyword);

    /**
     * 部署番号を条件に、その部署に所属する社員を取得します。
     * @param deptno 部署番号
     * @return 社員のリスト
     */
    List<Employee> findByDeptNo(Integer deptno);

    /**
     * 従業員情報を新規登録または更新します。
     * @param employee 保存対象のエンティティ
     * @return 保存後のエンティティ
     */
    Employee saveEmployee(Employee employee);

    /**
     * 社員番号を指定して 1 件の従業員を取得します。
     * @param empno 社員番号
     * @return 該当する従業員エンティティ
     */
    Employee findByEmployee(int empno);

    /**
     * 指定した社員番号の従業員を削除します。
     * @param empno 削除対象の社員番号
     */
    void deleteEmployeeById(Integer empno);

    /**
     * 指定した社員番号の従業員情報を検索し、
     * 取得したエンティティの内容を画面入力用の {@link EmployeeForm}
     * オブジェクトへコピーします。
     * <p>
     * 更新画面で初期表示する際などに使用します。
     * @param empno 社員番号
     * @param employeeForm コピー先フォーム
     * @return コピー後のフォーム
     */
    EmployeeForm findByEmpNoAndCopyToEmployeeForm(int empno, EmployeeForm employeeForm);

}
