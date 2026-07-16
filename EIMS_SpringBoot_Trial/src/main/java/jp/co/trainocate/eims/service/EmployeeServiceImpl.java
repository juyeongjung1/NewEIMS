package jp.co.trainocate.eims.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.repository.DepartmentRepository;
import jp.co.trainocate.eims.repository.EmployeeRepository;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(Integer empNo) {
        return employeeRepository.findById(empNo).orElse(null);
    }

    @Override
    public List<Employee> findByDeptNo(Integer deptNo) {
        return employeeRepository.findByDeptNo(deptNo);
    }

    @Override
    public List<Employee> findByEmpName(String name) {
        return employeeRepository.findByLastNameContainingOrFirstNameContaining(name, name);
    }

    @Override
    public Employee save(EmployeeForm form) {
        Employee employee = new Employee();
        copyFormToEntity(form, employee);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(EmployeeForm form) {
        Employee employee = employeeRepository.findById(form.getEmpNo()).orElse(null);
        if (employee != null) {
            copyFormToEntity(form, employee);
            return employeeRepository.save(employee);
        }
        return null;
    }

    @Override
    public void deleteById(Integer empNo) {
        employeeRepository.deleteById(empNo);
    }

    private void copyFormToEntity(EmployeeForm form, Employee employee) {
        employee.setLastName(form.getLastName());
        employee.setFirstName(form.getFirstName());
        employee.setLastKana(form.getLastKana());
        employee.setFirstKana(form.getFirstKana());
        employee.setPassword(form.getPassword());
        employee.setGender(form.getGender());
        
        Department department = departmentRepository.findById(form.getDeptNo()).orElse(null);
        employee.setDepartment(department);
    }
}
