package jp.co.trainocate.eims.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@Controller
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    /** トップページを表示する */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    /** 社員一覧を表示する */
    @GetMapping("/employeeList")
    public String showEmployeeList(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employee_list";
    }

    /** 退職者一覧を表示する */
    @GetMapping("/retireeList")
    public String showRetireeList(Model model) {
        model.addAttribute("retirees", employeeService.findRetirees());
        return "retiree_list";
    }

    /** 復元処理を行う */
    @PostMapping("/restore/{empNo}")
    public String restore(@PathVariable Integer empNo) {
        employeeService.restoreById(empNo);
        return "redirect:/retireeList";
    }

    /** 物理削除処理を行う */
    @PostMapping("/physicalDelete/{empNo}")
    public String physicalDelete(@PathVariable Integer empNo) {
        employeeService.physicalDeleteById(empNo);
        return "redirect:/retireeList";
    }

    /** 検索画面を表示する */
    @GetMapping("/search")
    public String showSearch(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "search";
    }

    /** 社員番号・氏名・部署番号で検索 */
    @GetMapping({"/selectByEmpNo", "/selectByEmpName", "/selectByDeptNo"})
    public String search(Integer empNo, String keyword, Integer deptNo, Model model) {
        if (empNo != null) {
            Employee employee = employeeService.findById(empNo);
            if (employee != null && employee.getDeleteFlg() == 0) {
                // ヒットした場合は詳細画面を直接表示
                model.addAttribute("employee", employee);
                return "employee_detail";
            }
            // ヒットしない場合は結果画面へ（0件表示用）
            model.addAttribute("employees", new ArrayList<Employee>());
            return "search_result";
        }

        if (keyword != null && !keyword.isBlank()) {
            List<Employee> employees = employeeService.findByEmpName(keyword);
            model.addAttribute("employees", employees);
            return "search_result";
        }

        if (deptNo != null) {
            List<Employee> employees = employeeService.findByDeptNo(deptNo);
            model.addAttribute("employees", employees);
            return "search_result";
        }

        model.addAttribute("departments", departmentService.findAll());
        return "search";
    }

    /** 社員詳細を表示する */
    @GetMapping("/detail/{empNo}")
    public String showDetail(@PathVariable Integer empNo, Model model) {
        Employee employee = employeeService.findById(empNo);
        model.addAttribute("employee", employee);
        return "employee_detail";
    }

    /** 登録画面を表示する（初回表示・GET） */
    @GetMapping("/input")
    public String showInputPage(EmployeeForm employeeForm, Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "input";
    }

    /** 確認画面から「修正する」で登録画面へ戻る（POST・入力値を保持） */
    @PostMapping("/input")
    public String backToInputPage(EmployeeForm employeeForm, Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "input";
    }

    /** 登録内容を確認する */
    @PostMapping("/inputConfirm")
    public String confirmRegistration(@Valid EmployeeForm employeeForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.findAll());
            return "input";
        }
        // 部署名を表示するために部署情報を取得
        model.addAttribute("department", departmentService.findById(employeeForm.getDeptNo()));
        return "input_confirm";
    }

    /** 新規社員を保存する */
    @PostMapping("/saveEmployee")
    public String saveEmployee(EmployeeForm employeeForm, Model model) {
        Employee employee = employeeService.save(employeeForm);
        model.addAttribute("employee", employee);
        return "input_complete";
    }

    /** 削除確認画面を表示する */
    @GetMapping("/deleteConfirm/{empNo}")
    public String deleteConfirm(@PathVariable Integer empNo, Model model) {
        Employee employee = employeeService.findById(empNo);
        if (employee == null) {
            model.addAttribute("employees", new ArrayList<Employee>());
            model.addAttribute("message", "指定された社員情報は存在しないため、削除できません。");
            return "search_result";
        }
        model.addAttribute("employee", employee);
        return "delete_confirm";
    }

    /** 社員を退職状態にする（論理削除） */
    @PostMapping("/deleteEmployee")
    public String deleteEmployee(Integer empNo, Model model) {
        if (empNo == null || employeeService.findById(empNo) == null) {
            model.addAttribute("employees", new ArrayList<Employee>());
            model.addAttribute("message", "指定された社員情報は存在しないため、削除できません。");
            return "search_result";
        }
        employeeService.deleteById(empNo);
        return "delete_complete";
    }

    /** 変更画面を表示する（初回表示・GET：DBから現在の登録内容をフォームにセット） */
    @GetMapping("/changeInput/{empNo}")
    public String changeInput(@PathVariable Integer empNo, EmployeeForm employeeForm, Model model) {
        Employee employee = employeeService.findById(empNo);
        employeeForm.setEmpNo(employee.getEmpNo());
        employeeForm.setLastName(employee.getLastName());
        employeeForm.setFirstName(employee.getFirstName());
        employeeForm.setLastKana(employee.getLastKana());
        employeeForm.setFirstKana(employee.getFirstKana());
        employeeForm.setPassword(employee.getPassword());
        employeeForm.setGender(employee.getGender());
        employeeForm.setDeptNo(employee.getDeptNo());

        model.addAttribute("departments", departmentService.findAll());
        return "change";
    }

    /** 確認画面から「修正する」で変更画面へ戻る（POST：DBから取り直さず入力値を保持） */
    @PostMapping("/changeInput/{empNo}")
    public String backToChangeInput(@PathVariable Integer empNo, EmployeeForm employeeForm, Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "change";
    }

    /** 変更内容を確認する */
    @PostMapping("/changeConfirm")
    public String changeConfirm(@Valid EmployeeForm employeeForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.findAll());
            return "change";
        }
        model.addAttribute("department", departmentService.findById(employeeForm.getDeptNo()));
        return "change_confirm";
    }

    /** 社員情報を更新する */
    @PostMapping("/changeEmployee")
    public String changeEmployee(EmployeeForm employeeForm, Model model) {
        Employee employee = employeeService.update(employeeForm);
        model.addAttribute("employee", employee);
        return "change_complete";
    }

}
