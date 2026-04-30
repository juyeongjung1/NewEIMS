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
        return employeeRepository.findAll();
    }

    @Override
    public List<Employee> findByEmpNo(Integer empNo) {
        return employeeRepository.findByEmpNo(empNo);
    }

    @Override
    public List<Employee> findByEmpName(String keyword) {
        return employeeRepository.findByLNameContainingOrFNameContaining(keyword, keyword);
    }

    @Override
    public List<Employee> findByDeptNo(Integer deptNo) {
        return employeeRepository.findByDeptNo(deptNo);
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
        employee.setLName(employeeForm.getLName());
        employee.setFName(employeeForm.getFName());
        employee.setLKana(employeeForm.getLKana());
        employee.setFKana(employeeForm.getFKana());
        employee.setPassword(employeeForm.getPassword());
        employee.setGender(employeeForm.getGender());
        employee.setDeptNo(employeeForm.getDeptNo());

        return employeeRepository.save(employee);
    }

    @Override
    public void deleteById(Integer empNo) {
        employeeRepository.deleteById(empNo);
    }
}
