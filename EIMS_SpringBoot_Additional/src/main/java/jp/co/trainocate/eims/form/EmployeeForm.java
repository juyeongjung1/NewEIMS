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
    private Integer empNo;
	
    /** 氏 */
    @NotBlank(message = "氏は必須項目です")
    @Size(max = 10, message = "氏は10文字以内で入力してください")
    private String lastName;

    /** 名 */
    @NotBlank(message = "名は必須項目です")
    @Size(max = 10, message = "名は10文字以内で入力してください")
    private String firstName;

    /** 氏（カナ） */
    @NotBlank(message = "氏（カナ）は必須項目です")
    @Size(max = 20, message = "氏（カナ）は20文字以内で入力してください")
    private String lastKana;

    /** 名（カナ） */
    @NotBlank(message = "名（カナ）は必須項目です")
    @Size(max = 20, message = "名（カナ）は20文字以内で入力してください")
    private String firstKana;

    /** パスワード */
    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 4, max = 16, message = "パスワードは4文字以上16文字以内で入力してください")
    private String password;

    /** 性別 1:男性 2:女性 */
    @NotNull(message = "性別を選択してください")
    private Integer gender;

    /** 部署番号 */
    @NotNull(message = "部署を選択してください")
    private Integer deptNo;

    /** 権限 0:一般 1:管理者 */
    private Integer role = 0;

    /** 削除フラグ 0:在籍 1:退職 */
    private Integer deleteFlg = 0;
}
