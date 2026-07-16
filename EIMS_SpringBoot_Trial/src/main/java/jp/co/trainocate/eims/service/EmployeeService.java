package jp.co.trainocate.eims.service;

import java.util.List;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;

public interface EmployeeService {
    List<Employee> findAll();
    Employee findById(Integer empNo);
    List<Employee> findByDeptNo(Integer deptNo);
    List<Employee> findByEmpName(String name);
    Employee save(EmployeeForm form);
    Employee update(EmployeeForm form);
    void deleteById(Integer empNo);
}
