package jp.co.trainocate.eims.entity;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "department")
@Data
@ToString(exclude = "employees")
public class Department {

    @Id
    @Column(name = "dept_no")
    private Integer deptNo;

    @Column(name = "dept_name", length = 10, nullable = false)
    private String deptName;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
}
