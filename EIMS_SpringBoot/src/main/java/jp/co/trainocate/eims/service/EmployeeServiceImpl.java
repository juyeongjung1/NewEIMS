package jp.co.trainocate.eims.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * EmployeeService の実装クラス。
 * Repository を利用して従業員情報を操作します。
 */
public class EmployeeServiceImpl implements EmployeeService {

        /** EmployeeRepository のDI */
        @Autowired
        private final EmployeeRepository employeeRepository;

        /** 社員番号で検索 */
        @Override
        public List<Employee> findByEmpNo(int empno) {
                // Repository を利用して主キー検索を実行
                return employeeRepository.findByempno(empno);
        }

        /** 氏名で検索 */
        @Override
        public List<Employee> findByEmpName(String keyword) {
                // あいまい検索のためキーワードを % で挟む
                return employeeRepository.findByEmpNameLike("%" + keyword + "%");
        }

	//JPQLを使わないパターン
	/*public List<Employee> findByEmpName(String keyword) {
		String likeKeyword = "%" + keyword + "%";
		return employeeRepository.findByLnameLikeOrFnameLikeOrLkanaLikeOrFkanaLike(likeKeyword, likeKeyword,
				likeKeyword, likeKeyword);
	}*/
        /** 部署番号で検索 */
        @Override
        public List<Employee> findByDeptNo(Integer deptno) {
                // 部署番号を条件に所属する従業員を取得
                return employeeRepository.findByDepartmentDeptno(deptno);
        }

    /** 従業員を保存 */
    @Override
    public Employee saveEmployee(Employee employee) {
        // save メソッドは新規登録と更新の両方に利用可能
        return employeeRepository.save(employee);
    }

        /** 社員番号をキーに従業員を取得 */
        @Override
        public Employee findByEmployee(int empno) {
                // Optional を取得し、中身を取り出す
                return employeeRepository.findById(empno).get();
        }

        /** 従業員を削除 */
        @Override
        public void deleteEmployeeById(Integer empno) {
                // 指定された社員番号で削除処理を実行
                employeeRepository.deleteById(empno);
        }

        /**
         * 社員番号で検索し、結果を EmployeeForm にコピーして返却。
         */
        @Override
        public EmployeeForm findByEmpNoAndCopyToEmployeeForm(int empno, EmployeeForm employeeForm) {
                // DB から従業員を取得
                Employee employee = employeeRepository.findById(empno).get();
                // 取得したエンティティの内容をフォームにコピー
                employeeForm.setEmpno(employee.getEmpno());
                employeeForm.setFname(employee.getFname());
                employeeForm.setLname(employee.getLname());
                employeeForm.setFkana(employee.getFkana());
                employeeForm.setLkana(employee.getLkana());
                employeeForm.setGender(employee.getGender());
                employeeForm.setDeptno(employee.getDeptno());
                employeeForm.setPassword(employee.getPassword());
                return employeeForm;
        }
}
