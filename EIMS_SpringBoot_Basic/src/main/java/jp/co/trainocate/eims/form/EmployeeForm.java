package jp.co.trainocate.eims.form;

/**
 * 画面入力用の従業員フォーム。
 * バリデーションアノテーションを使用して入力チェックを行います。
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeForm {
	
    // 新規登録時は null、更新時は既存の社員番号が入る
    private Integer empno;
	
    /** 氏 */
    @NotBlank(message = "氏は必須項目です")
    @Size(max = 10, message = "氏は10文字以内で入力してください")
    private String lname;

    /** 名 */
    @NotBlank(message = "名は必須項目です")
    @Size(max = 10, message = "名は10文字以内で入力してください")
    private String fname;

    /** 氏（カナ） */
    @NotBlank(message = "氏（カナ）は必須項目です")
    @Size(max = 10, message = "氏（カナ）は10文字以内で入力してください")
    private String lkana;

    /** 名（カナ） */
    @NotBlank(message = "名（カナ）は必須項目です")
    @Size(max = 10, message = "名（カナ）は10文字以内で入力してください")
    private String fkana;

    /** パスワード */
    @NotBlank(message = "パスワードは必須項目です")
    @Size(max = 8, message = "パスワードは8文字以内で入力してください")
    private String password;

    /** 性別 1:男性 2:女性 */
    @NotNull(message = "性別は必須項目です")
    private Integer gender;

    /** 部署番号 */
    private Integer deptno;
}
