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
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public List<Employee> findByEmpno(Integer empno) {
        return employeeRepository.findByEmpno(empno);
    }

    @Override
    public List<Employee> findByEmpName(String keyword) {
        return employeeRepository.findByLnameContainingOrFnameContaining(keyword, keyword);
    }

    @Override
    public List<Employee> findByDeptno(Integer deptno) {
        return employeeRepository.findByDeptno(deptno);
    }

    @Override
    public Employee findById(Integer empno) {
        return employeeRepository.findById(empno).orElse(null);
    }

    @Override
    public Employee save(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        // 更新の場合は既存のエンティティを取得
        if (employeeForm.getEmpno() != null) {
            employee = employeeRepository.findById(employeeForm.getEmpno()).orElse(new Employee());
        }

        // フォームからエンティティへ手動で詰め替え
        employee.setEmpno(employeeForm.getEmpno());
        employee.setLname(employeeForm.getLname());
        employee.setFname(employeeForm.getFname());
        employee.setLkana(employeeForm.getLkana());
        employee.setFkana(employeeForm.getFkana());
        employee.setPassword(employeeForm.getPassword());
        employee.setGender(employeeForm.getGender());
        employee.setDeptno(employeeForm.getDeptno());

        return employeeRepository.save(employee);
    }

    @Override
    public void deleteById(Integer empno) {
        employeeRepository.deleteById(empno);
    }
}

