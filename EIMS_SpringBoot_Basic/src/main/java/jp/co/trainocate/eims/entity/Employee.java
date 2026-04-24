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
    // 主キーに自動採番を使用
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 社員番号 */
    private Integer empno;
	
    /** 氏 */
    @Column(length = 10, nullable = false)
    private String lname;
	
    /** 名 */
    @Column(length = 10, nullable = false)
    private String fname;
	
    /** 氏(カナ) */
    @Column(length = 20, nullable = false)
    private String lkana;
	
    /** 名(カナ) */
    @Column(length = 20, nullable = false)
    private String fkana;
	
    /** パスワード */
    @Column(length = 20, nullable = false)
    private String password;
	
    /** 性別 1:男性 2:女性 */
    @Column(nullable = false)
    private Integer gender;
	
    /** 部署番号 (外部キー) */
    @Column(name="deptno")
    private Integer deptno;
	
    /** 所属部署 */
    @ManyToOne
    @JoinColumn(name = "deptno", referencedColumnName = "deptno", insertable = false, updatable = false)
    private Department department;
	
}
