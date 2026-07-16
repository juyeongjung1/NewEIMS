package jp.co.trainocate.eims.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jp.co.trainocate.eims.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByDeptNo(Integer deptNo);
    List<Employee> findByLastNameContainingOrFirstNameContaining(String lastName, String firstName);
}
