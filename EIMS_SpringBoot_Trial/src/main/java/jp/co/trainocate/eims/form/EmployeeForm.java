package jp.co.trainocate.eims.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeForm {

    private Integer empNo;

    @NotBlank(message = "{V001}")
    @Size(max = 10, message = "{V002}")
    private String lastName;

    @NotBlank(message = "{V003}")
    @Size(max = 10, message = "{V004}")
    private String firstName;

    @NotBlank(message = "{V005}")
    @Size(max = 20, message = "{V006}")
    private String lastKana;

    @NotBlank(message = "{V007}")
    @Size(max = 20, message = "{V008}")
    private String firstKana;

    @NotBlank(message = "{V009}")
    @Size(min = 4, max = 16, message = "{V010}")
    private String password;

    @NotNull(message = "{V011}")
    private Integer gender;

    @NotNull(message = "{V012}")
    private Integer deptNo;
}
