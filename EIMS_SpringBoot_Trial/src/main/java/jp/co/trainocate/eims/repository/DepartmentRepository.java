package jp.co.trainocate.eims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jp.co.trainocate.eims.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
