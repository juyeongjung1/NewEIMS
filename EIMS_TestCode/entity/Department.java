package jp.co.trainocate.enshu.entity;

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

@Entity
@Data
@Table(name = "department")
public class Department {
    // 主キーの部署番号。
    @Id
    /** 部署番号 */
    private Integer deptno;
	
    /** 部署名 */
    @Column(length = 10, nullable = false)
    private String deptname;
	
    /** 部署に所属する社員一覧 */
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
    

    // ★追加：部門番号・部門名を同時設定するコンストラクタ
    public Department(Integer deptno, String deptname) {
        super();
    	this.deptno = deptno;
        this.deptname = deptname;
    }
    
 
    // ★JPA用のデフォルトコンストラクタ（必須）
    public Department() {}
}
