package jp.co.trainocate.eims.entity;

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
@Table(name = "employee")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_no")
    private Integer empNo;

    @Column(name = "last_name", length = 10, nullable = false)
    private String lastName;

    @Column(name = "first_name", length = 10, nullable = false)
    private String firstName;

    @Column(name = "last_kana", length = 20, nullable = false)
    private String lastKana;

    @Column(name = "first_kana", length = 20, nullable = false)
    private String firstKana;

    @Column(name = "password", length = 20, nullable = false)
    private String password;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "dept_no", insertable = false, updatable = false, nullable = false)
    private Integer deptNo;

    @ManyToOne
    @JoinColumn(name = "dept_no", nullable = false)
    private Department department;

    @Column(name = "role", nullable = false)
    private Integer role = 0;

    @Column(name = "delete_flg", nullable = false)
    private Integer deleteFlg = 0;
}
