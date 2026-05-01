package jp.co.trainocate.eims.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public List<Employee> findAll() {
        // 通常の全件取得も在籍者のみとする
        return employeeRepository.findByDeleteFlg(0);
    }

    @Override
    public List<Employee> findByEmpNo(Integer empNo) {
        return employeeRepository.findByEmpNoAndDeleteFlg(empNo, 0);
    }

    @Override
    public List<Employee> findByEmpName(String keyword) {
        return employeeRepository.findByLastNameContainingOrFirstNameContainingAndDeleteFlg(keyword, keyword, 0);
    }

    @Override
    public List<Employee> findByDeptNo(Integer deptNo) {
        return employeeRepository.findByDeptNoAndDeleteFlg(deptNo, 0);
    }

    @Override
    public Employee findById(Integer empNo) {
        return employeeRepository.findById(empNo).orElse(null);
    }

    @Override
    public Employee save(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        // 更新の場合は既存のエンティティを取得
        if (employeeForm.getEmpNo() != null) {
            employee = employeeRepository.findById(employeeForm.getEmpNo()).orElse(new Employee());
        }

        // フォームからエンティティへ手動で詰め替え
        employee.setEmpNo(employeeForm.getEmpNo());
        employee.setLastName(employeeForm.getLastName());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastKana(employeeForm.getLastKana());
        employee.setFirstKana(employeeForm.getFirstKana());
        employee.setPassword(employeeForm.getPassword());
        employee.setGender(employeeForm.getGender());
        employee.setDeptNo(employeeForm.getDeptNo());
        
        // 追加フィールドの詰め替え
        if (employeeForm.getRole() != null) {
            employee.setRole(employeeForm.getRole());
        }
        if (employeeForm.getDeleteFlg() != null) {
            employee.setDeleteFlg(employeeForm.getDeleteFlg());
        }

        return employeeRepository.save(employee);
    }

    @Override
    public void deleteById(Integer empNo) {
        // 論理削除：フラグを1に更新する
        Employee employee = employeeRepository.findById(empNo).orElse(null);
        if (employee != null) {
            employee.setDeleteFlg(1);
            employeeRepository.save(employee);
        }
    }

    @Override
    public List<Employee> findRetirees() {
        return employeeRepository.findByDeleteFlg(1);
    }

    @Override
    public void physicalDeleteById(Integer empNo) {
        employeeRepository.deleteById(empNo);
    }

    @Override
    public void restoreById(Integer empNo) {
        Employee employee = employeeRepository.findById(empNo).orElse(null);
        if (employee != null) {
            employee.setDeleteFlg(0);
            employeeRepository.save(employee);
        }
    }
}
