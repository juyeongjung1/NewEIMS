package jp.co.trainocate.eims.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginForm {
    @NotNull(message = "社員番号を入力してください")
    private Integer empNo;

    @NotNull(message = "パスワードを入力してください")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String password;
}
