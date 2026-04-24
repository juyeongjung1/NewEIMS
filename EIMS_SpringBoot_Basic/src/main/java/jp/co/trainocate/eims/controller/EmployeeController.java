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

    /** 社員一覧を表示する */
    @GetMapping("/employeeList")
    public String showEmployeeList(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employee_list";
    }

    /** 検索画面を表示する */
    @GetMapping("/search")
    public String showSearchPage(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "search";
    }

    /** 社員番号で検索 */
    @GetMapping("/selectByEmpNo")
    public String selectByEmpNo(Integer empno, Model model) {
        if (empno == null) {
            return "search";
        }
        Employee employee = employeeService.findById(empno);
        if (employee != null) {
            // ヒットした場合は詳細画面を直接表示
            model.addAttribute("employee", employee);
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
    public String selectByDeptNo(Integer deptno, Model model) {
        if (deptno == null) {
            return "search";
        }
        List<Employee> employees = employeeService.findByDeptno(deptno);
        model.addAttribute("employees", employees);
        return "search_result";
    }

    /** 社員詳細を表示する */
    @GetMapping("/detail/{empno}")
    public String showDetail(@PathVariable("empno") Integer empno, Model model) {
        Employee employee = employeeService.findById(empno);
        model.addAttribute("employee", employee);
        return "employee_detail";
    }

    /** 登録画面を表示する */
    @GetMapping("/input")
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
        model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
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
    @GetMapping("/deleteConfirm/{empno}")
    public String deleteConfirm(@PathVariable("empno") Integer empno, Model model) {
        Employee employee = employeeService.findById(empno);
        model.addAttribute("employee", employee);
        return "delete_confirm";
    }

    /** 社員を削除する */
    @PostMapping("/deleteEmployee")
    public String deleteEmployee(Integer empno) {
        employeeService.deleteById(empno);
        return "delete_complete";
    }

    /** 変更入力画面を表示する */
    @GetMapping("/changeInput/{empno}")
    public String changeInput(@PathVariable("empno") Integer empno, EmployeeForm employeeForm, Model model) {
        // 初回アクセス（GET）の場合のみ、DBからデータを取得してフォームにセット
        if (employeeForm.getLname() == null) {
            Employee employee = employeeService.findById(empno);
            employeeForm.setEmpno(employee.getEmpno());
            employeeForm.setLname(employee.getLname());
            employeeForm.setFname(employee.getFname());
            employeeForm.setLkana(employee.getLkana());
            employeeForm.setFkana(employee.getFkana());
            employeeForm.setPassword(employee.getPassword());
            employeeForm.setGender(employee.getGender());
            employeeForm.setDeptno(employee.getDeptno());
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
        model.addAttribute("department", departmentService.findById(employeeForm.getDeptno()));
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

