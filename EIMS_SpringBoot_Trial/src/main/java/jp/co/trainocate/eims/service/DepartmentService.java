package jp.co.trainocate.eims.service;

import java.util.List;
import jp.co.trainocate.eims.entity.Department;

public interface DepartmentService {
    List<Department> findAll();
    Department findById(Integer deptNo);
}
