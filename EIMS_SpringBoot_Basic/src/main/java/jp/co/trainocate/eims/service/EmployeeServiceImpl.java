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
        return employeeRepository.findByLastNameContainingOrFirstNameContaining(keyword, keyword);
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
        Employee employee = copyToEmployee(new Employee(), employeeForm);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(EmployeeForm employeeForm) {
        Employee employee = employeeRepository.findById(employeeForm.getEmpNo()).orElse(new Employee());
        copyToEmployee(employee, employeeForm);

        return employeeRepository.save(employee);
    }

    @Override
    public void deleteById(Integer empNo) {
        employeeRepository.deleteById(empNo);
    }

    private Employee copyToEmployee(Employee employee, EmployeeForm employeeForm) {
        employee.setEmpNo(employeeForm.getEmpNo());
        employee.setLastName(employeeForm.getLastName());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastKana(employeeForm.getLastKana());
        employee.setFirstKana(employeeForm.getFirstKana());
        employee.setPassword(employeeForm.getPassword());
        employee.setGender(employeeForm.getGender());
        employee.setDeptNo(employeeForm.getDeptNo());
        return employee;
    }
}
