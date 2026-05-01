package jp.co.trainocate.eims.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.validation.Valid;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.service.DepartmentService;
import jp.co.trainocate.eims.service.EmployeeService;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DepartmentService departmentService;

    /** トップページを表示する */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    /** 社員一覧を表示する（在籍者のみ） */
    @GetMapping("/employeeList")
    public String showEmployeeList(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employee_list";
    }

    /** 退職者一覧を表示する */
    @GetMapping("/retireeList")
    public String showRetireeList(Model model) {
        model.addAttribute("employees", employeeService.findRetirees());
        return "retiree_list";
    }

    /** 復元処理を行う */
    @PostMapping("/restore/{empNo}")
    public String restore(@PathVariable("empNo") Integer empNo) {
        employeeService.restoreById(empNo);
        return "redirect:/retireeList";
    }

    /** 物理削除処理を行う */
    @PostMapping("/physicalDelete/{empNo}")
    public String physicalDelete(@PathVariable("empNo") Integer empNo) {
        employeeService.physicalDeleteById(empNo);
        return "redirect:/retireeList";
    }

    /** 検索画面を表示する */
    @GetMapping("/search")
    public String showSearchPage(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "search";
    }

    /** 社員番号で検索 */
    @GetMapping("/selectByEmpNo")
    public String selectByEmpNo(Integer empNo, Model model) {
        if (empNo == null) {
            return "search";
        }
        List<Employee> employees = employeeService.findByEmpNo(empNo);
        if (employees.size() == 1) {
            // ヒットした場合は詳細画面を直接表示
            model.addAttribute("employee", employees.get(0));
            return "employee_detail";
        }
        // ヒットしない場合は結果画面へ（0件表示用）
        model.addAttribute("employees", new ArrayList<Employee>());
        return "search_result";
    }

    /** 名字または名前で検索 */
    @GetMapping("/selectByEmpName")
    public String selectByEmpName(String keyword, Model model) {
        if (keyword == null || keyword.isBlank()) {
            return "search";
        }
        List<Employee> employees = employeeService.findByEmpName(keyword);
        model.addAttribute("employees", employees);
        return "search_result";
    }

    /** 部署番号で検索 */
    @GetMapping("/selectByDeptNo")
    public String selectByDeptNo(Integer deptNo, Model model) {
        if (deptNo == null) {
            return "search";
        }
        List<Employee> employees = employeeService.findByDeptNo(deptNo);
        model.addAttribute("employees", employees);
        return "search_result";
    }

    /** 社員詳細を表示する */
    @GetMapping("/detail/{empNo}")
    public String showDetail(@PathVariable("empNo") Integer empNo, Model model) {
        Employee employee = employeeService.findById(empNo);
        model.addAttribute("employee", employee);
        return "employee_detail";
    }

    /** 登録画面を表示する */
    @RequestMapping(value = "/input", method = {RequestMethod.GET, RequestMethod.POST})
    public String showInputPage(EmployeeForm employeeForm, Model model) {
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
    public String deleteConfirm(@PathVariable("empNo") Integer empNo, Model model) {
        Employee employee = employeeService.findById(empNo);
        model.addAttribute("employee", employee);
        return "delete_confirm";
    }

    /** 社員を削除（論理削除）する */
    @PostMapping("/deleteEmployee")
    public String deleteEmployee(Integer empNo) {
        employeeService.deleteById(empNo);
        return "delete_complete";
    }

    /** 変更入力画面を表示する */
    @RequestMapping(value = "/changeInput/{empNo}", method = {RequestMethod.GET, RequestMethod.POST})
    public String changeInput(@PathVariable("empNo") Integer empNo, EmployeeForm employeeForm, Model model) {
        // 初回アクセス（GET）の場合のみ、DBからデータを取得してフォームにセット
        if (employeeForm.getLastName() == null) {
            Employee employee = employeeService.findById(empNo);
            employeeForm.setEmpNo(employee.getEmpNo());
            employeeForm.setLastName(employee.getLastName());
            employeeForm.setFirstName(employee.getFirstName());
            employeeForm.setLastKana(employee.getLastKana());
            employeeForm.setFirstKana(employee.getFirstKana());
            employeeForm.setPassword(employee.getPassword());
            employeeForm.setGender(employee.getGender());
            employeeForm.setDeptNo(employee.getDeptNo());
            
            // 追加フィールドのセット
            employeeForm.setRole(employee.getRole());
            employeeForm.setDeleteFlg(employee.getDeleteFlg());
        }

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
        Employee employee = employeeService.save(employeeForm);
        model.addAttribute("employee", employee);
        return "change_complete";
    }
}
