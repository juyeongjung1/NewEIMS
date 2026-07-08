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

    /** {@inheritDoc} */
    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public List<Employee> findByEmpName(String keyword) {
        return employeeRepository.findByDeleteFlgAndLastNameContainingOrDeleteFlgAndFirstNameContaining(
                0, keyword, 0, keyword);
    }

    /** {@inheritDoc} */
    @Override
    public List<Employee> findByDeptNo(Integer deptNo) {
        return employeeRepository.findByDeptNoAndDeleteFlg(deptNo, 0);
    }

    /** {@inheritDoc} */
    @Override
    public Employee findById(Integer empNo) {
        return employeeRepository.findById(empNo).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Employee save(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        employee.setEmpNo(employeeForm.getEmpNo());
        employee.setLastName(employeeForm.getLastName());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastKana(employeeForm.getLastKana());
        employee.setFirstKana(employeeForm.getFirstKana());
        employee.setPassword(employeeForm.getPassword());
        employee.setGender(employeeForm.getGender());
        employee.setDeptNo(employeeForm.getDeptNo());
        employee.setRole(employeeForm.getRole());
        employee.setDeleteFlg(employeeForm.getDeleteFlg());

        return employeeRepository.save(employee);
    }

    /** {@inheritDoc} */
    @Override
    public Employee update(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        employee.setEmpNo(employeeForm.getEmpNo());
        employee.setLastName(employeeForm.getLastName());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastKana(employeeForm.getLastKana());
        employee.setFirstKana(employeeForm.getFirstKana());
        employee.setPassword(employeeForm.getPassword());
        employee.setGender(employeeForm.getGender());
        employee.setDeptNo(employeeForm.getDeptNo());
        employee.setRole(employeeForm.getRole());
        employee.setDeleteFlg(employeeForm.getDeleteFlg());

        return employeeRepository.save(employee);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteById(Integer empNo) {
        Employee employee = employeeRepository.findById(empNo).orElse(null);
        if (employee != null) {
            employee.setDeleteFlg(1);
            employeeRepository.save(employee);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Employee> findRetirees() {
        return employeeRepository.findByDeleteFlg(1);
    }

    /** {@inheritDoc} */
    @Override
    public void physicalDeleteById(Integer empNo) {
        employeeRepository.deleteById(empNo);
    }

    /** {@inheritDoc} */
    @Override
    public void restoreById(Integer empNo) {
        Employee employee = employeeRepository.findById(empNo).orElse(null);
        if (employee != null) {
            employee.setDeleteFlg(0);
            employeeRepository.save(employee);
        }
    }
}
