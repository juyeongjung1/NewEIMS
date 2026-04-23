package jp.co.trainocate.eims.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@Controller
/**
 * 社員情報の登録・検索・更新・削除に関する画面遷移を制御するコントローラです。
 * <p>
 * 画面からのリクエストを受け取り、サービス層へ処理を委譲します。
 */
public class EmployeeController {

    /** EmployeeService のDI */
    @Autowired
    private EmployeeService employeeService;

    /** DepartmentService のDI */
    @Autowired
    private DepartmentService departmentService;

    /** トップページを表示する */
    @GetMapping("/index")
    public String index() {
            // index.html をそのまま表示するだけなので特別な処理は不要
            return "index";
    }

    /** 検索画面を表示する */
    @GetMapping("/search")
    public String showSearchPage(Model model) {
            // プルダウンに部署一覧を表示するため、事前に全件取得して Model にセット
            model.addAttribute("departments", departmentService.findAll());
            return "search";
    }

    /** 社員番号で検索 */
    @GetMapping("/selectByEmpNo")
    public String selectByEmpNo(Integer empno, Model model) {
                // パラメータが null の場合は検索画面に戻す
                if (empno != null) {
                        // サービス層で社員番号を条件に検索
                        List<Employee> employees = employeeService.findByEmpNo(empno);
                        // 検索結果を画面で利用できるよう Model に登録
                        model.addAttribute("employees", employees);
                        // 結果表示画面へ遷移
                        return "search_result";
                }
                // 社員番号が指定されなかった場合は検索画面へ
                return "search";
        }

    /** 氏名で検索 */
    @GetMapping("/selectByEmpName")
    public String selectByEmpName(String keyword, Model model) {
            // キーワードで部分一致検索を行う
            List<Employee> employees = employeeService.findByEmpName(keyword);
            // 検索結果を画面表示用にセット
            model.addAttribute("employees", employees);
            return "search_result";
    }

    /** 部署番号で検索 */
    @GetMapping("/selectByDeptNo")
    public String selectByDeptNo(Integer deptno, Model model) {
            // 部署番号で検索することで部署に所属する社員一覧を取得
            List<Employee> employees = employeeService.findByDeptNo(deptno);
            // 取得した社員情報をモデルに登録
            model.addAttribute("employees", employees);
            return "search_result";
    }

    /** 登録画面を表示する */
    @GetMapping("/input")
    public String showInputPage(EmployeeForm employeeForm, Model model) {
            // 部署一覧を取得し、ドロップダウン表示に利用
            model.addAttribute("departments", departmentService.findAll());
            // 画面遷移だけを行う
            return "input";
    }

    /** 入力内容を確認する */
    @PostMapping("/inputConfirm")
    public String confirmRegistration(@Valid EmployeeForm employeeForm, BindingResult bindingResult, Model model) {
                // 入力チェックでエラーがあれば再入力を促す
                if (bindingResult.hasErrors()) {
                        // エラー時も部署一覧を再度セットする必要がある
                        model.addAttribute("departments", departmentService.findAll());
                        return "input";
                }
                // 部署番号から部署名を取得して確認画面に表示
                model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
                return "input_confirm";
        }

    /** 新規社員を登録する */
    @PostMapping("/saveEmployee")
    public String saveEmployee(EmployeeForm employeeForm, ModelMapper modelMapper, Model model) {
                // 画面から受け取ったフォームオブジェクトをエンティティへ変換
                Employee employee = modelMapper.map(employeeForm, Employee.class);
                // DB へ保存し、採番された社員番号を取得
                employee = employeeService.saveEmployee(employee);
                employeeForm.setEmpno(employee.getEmpno());
                model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
                // 完了画面へ遷移
                return "input_complete";
        }

    /** 削除確認画面を表示する */
    @GetMapping("/deleteConfirm/{empno}")
    public String deleteConfirm(@PathVariable("empno") Integer empno, Model model) {
                // 削除対象の従業員を取得し確認画面で表示
                Employee employee = employeeService.findByEmployee(empno);
                model.addAttribute("employee", employee);
                return "delete_confirm";
        }

    /** 社員を削除する */
    @PostMapping("/deleteEmployee")
    public String deleteEmployee(EmployeeForm employeeForm, Model model) {
                // サービス層で削除処理を実行
                employeeService.deleteEmployeeById(employeeForm.getEmpno());
                model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
                return "delete_complete";
        }

    /** 変更入力画面を表示する */
    @GetMapping("/changeInput/{empno}")
    public String changeInput(@PathVariable("empno") int empno, EmployeeForm employeeForm, Model model) {
                // 既存の社員情報を取得し、フォームに詰め替えて画面に表示
                employeeForm = employeeService.findByEmpNoAndCopyToEmployeeForm(empno, employeeForm);
                // 変更画面でも部署選択が必要なため一覧を取得
                model.addAttribute("departments", departmentService.findAll());
                return "change";
        }

    /** 変更内容を確認する */
    @PostMapping("/changeConfirm")
    public String changeConfirm(@Valid EmployeeForm employeeForm, BindingResult bindingResult, Model model) {
                // 入力チェックでエラーがあれば再度入力画面へ
                if (bindingResult.hasErrors()) {
                        // 部署リストを再表示するためセット
                        model.addAttribute("departments", departmentService.findAll());
                        return "change";
                }
                // 部署名を取得し確認画面に表示
                model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
                return "change_confirm";
        }

    /** 社員情報を更新する */
    @PostMapping("/changeEmployee")
    public String changeEmployee(EmployeeForm employeeForm, ModelMapper modelMapper, Model model) {
                // 入力された内容でエンティティを生成し保存
                Employee employee = modelMapper.map(employeeForm, Employee.class);
                employee = employeeService.saveEmployee(employee);
                model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
                // 更新完了後は完了画面へ
                return "change_complete";
        }
}
