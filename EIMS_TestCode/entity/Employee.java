package jp.co.trainocate.enshu.entity;

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
	@Column(length = 20, nullable = false)
	private String lname;

	/** 名 */
	@Column(length = 20, nullable = false)
	private String fname;

	/** 氏(カナ) */
	@Column(length = 50, nullable = false)
	private String lkana;

	/** 名(カナ) */
	@Column(length = 50, nullable = false)
	private String fkana;

	/** パスワード */
	@Column(length = 30, nullable = false)
	private String password;

	/** 性別 1:男性 2:女性 */
	@Column(nullable = false)
	private Integer gender;

	/** 所属部署 */
	@ManyToOne
	@JoinColumn(name = "deptno", insertable = false, updatable = false)
	private Department department;

	/** 部署番号 (外部キー) */
	@Column(name = "deptno")
	private Integer deptno;

	// ★追加：全主要フィールドを同時設定するコンストラクタ。departmentを初期化するパターン
	public Employee(Integer empno, String lname, String fname, String lkana, String fkana,
			String password, Integer gender, Department department) {
		super();
		this.empno = empno;
		this.lname = lname;
		this.fname = fname;
		this.lkana = lkana;
		this.fkana = fkana;
		this.password = password;
		this.gender = gender;
		this.department = department;
	}

	// ★追加：全主要同時設定するコンストラクタ。deptnoを初期化するパターン
	public Employee(Integer empno, String lname, String fname, String lkana, String fkana,
			String password, Integer gender, Integer deptno) {
		super();
		this.empno = empno;
		this.lname = lname;
		this.fname = fname;
		this.lkana = lkana;
		this.fkana = fkana;
		this.password = password;
		this.gender = gender;
		this.deptno = deptno;
	}

	
		
	// ★JPA用のデフォルトコンストラクタ（必須）
	public Employee() {
	}
}
