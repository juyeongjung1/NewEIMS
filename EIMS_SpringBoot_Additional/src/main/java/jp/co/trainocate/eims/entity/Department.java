package jp.co.trainocate.eims.entity;

/**
 * 部署エンティティ。
 */

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "department")
public class Department {
    /** 部署番号 */
    @Id
    @Column(name = "dept_no")
    private Integer deptNo;
	
    /** 部署名 */
    @Column(name = "dept_name", length = 10, nullable = false)
    private String deptName;
	
    /** 部署に所属する社員一覧 */
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;

    // テスト用のコンストラクタ
    public Department(Integer deptNo, String deptName) {
        this.deptNo = deptNo;
        this.deptName = deptName;
    }
}
