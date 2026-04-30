package jp.co.trainocate.eims.entity;

/**
 * 従業員エンティティ。
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "employee")
public class Employee {
    /** 社員番号 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_no")
    private Integer empNo;
	
    /** 氏 */
    @Column(name = "last_name", length = 10, nullable = false)
    private String lastName;
	
    /** 名 */
    @Column(name = "first_name", length = 10, nullable = false)
    private String firstName;
	
    /** 氏(カナ) */
    @Column(name = "last_kana", length = 20, nullable = false)
    private String lastKana;
	
    /** 名(カナ) */
    @Column(name = "first_kana", length = 20, nullable = false)
    private String firstKana;
	
    /** パスワード */
    @Column(length = 20, nullable = false)
    private String password;
	
    /** 性別 1:男性 2:女性 */
    @Column(nullable = false)
    private Integer gender;
	
    /** 部署番号 (外部キー) */
    @Column(name = "dept_no")
    private Integer deptNo;
	
    /** 所属部署 */
    @ManyToOne
    @JoinColumn(name = "dept_no", referencedColumnName = "dept_no", insertable = false, updatable = false)
    private Department department;
	
}
