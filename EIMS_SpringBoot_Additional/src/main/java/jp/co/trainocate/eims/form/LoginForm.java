package jp.co.trainocate.eims.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginForm {
    @NotNull(message = "社員番号を入力してください")
    private Integer empNo;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 4, max = 16, message = "パスワードは4文字以上16文字以内で入力してください")
    private String password;
}
