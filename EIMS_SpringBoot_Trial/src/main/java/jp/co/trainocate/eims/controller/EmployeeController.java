package jp.co.trainocate.eims.controller;

import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import jp.co.trainocate.eims.entity.Department;
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

    @Autowired
    private MessageSource messageSource;

    @GetMapping({ "/", "/index" })
    public String index() {
        return "index";
    }

    @GetMapping("/employeeList")
    public String showEmployeeList(Model model) {
        List<Employee> employees = employeeService.findAll();
        model.addAttribute("employees", employees);
        return "employee_list";
    }

    @GetMapping("/detail/{empNo}")
    public String showDetail(@PathVariable("empNo") Integer empNo, Model model) {
        Employee employee = employeeService.findById(empNo);
        model.addAttribute("employee", employee);
        return "employee_detail";
    }

    @GetMapping("/search")
    public String searchIndex(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "search";
    }

    @GetMapping({ "/selectByEmpName", "/selectByDeptNo", "/selectByEmpNo" })
    public String search(
            @RequestParam(value = "empNo", required = false) Integer empNo,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "deptNo", required = false) Integer deptNo,
            Model model,
            Locale locale) {

        // 社員番号検索
        if (empNo != null) {
            Employee employee = employeeService.findById(empNo);
            if (employee != null) {
                model.addAttribute("employee", employee);
                return "employee_detail";
            } else {
                model.addAttribute("errorMessage", messageSource.getMessage("E001", null, locale));
                model.addAttribute("departments", departmentService.findAll());
                return "search";
            }
        }

        // 氏名検索
        if (name != null) {
            if (name.trim().isEmpty()) {
                model.addAttribute("errorMessage", "検索キーワードを入力してください。");
                model.addAttribute("departments", departmentService.findAll());
                return "search";
            }
            List<Employee> employees = employeeService.findByEmpName(name);
            model.addAttribute("employees", employees);
            if (employees.isEmpty()) {
                model.addAttribute("errorMessage", messageSource.getMessage("E001", null, locale));
            }
            return "search_result";
        }

        // 部署検索
        if (deptNo != null) {
            List<Employee> employees = employeeService.findByDeptNo(deptNo);
            model.addAttribute("employees", employees);
            if (employees.isEmpty()) {
                model.addAttribute("errorMessage", messageSource.getMessage("E001", null, locale));
            }
            return "search_result";
        }

        // 何も指定がない場合は検索画面に戻す
        model.addAttribute("departments", departmentService.findAll());
        return "search";
    }

    @RequestMapping(value = "/input", method = { RequestMethod.GET, RequestMethod.POST })
    public String showInputPage(@ModelAttribute("employeeForm") EmployeeForm form, Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "input";
    }

    @PostMapping("/inputConfirm")
    public String confirmRegistration(
            @Validated @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.findAll());
            return "input";
        }
        Department department = departmentService.findById(form.getDeptNo());
        model.addAttribute("department", department);
        return "input_confirm";
    }

    @PostMapping("/saveEmployee")
    public String saveEmployee(@ModelAttribute("employeeForm") EmployeeForm form, Model model) {
        Employee employee = employeeService.save(form);
        model.addAttribute("employee", employee);
        return "input_complete";
    }

    @RequestMapping(value = "/changeInput/{empNo}", method = { RequestMethod.GET, RequestMethod.POST })
    public String changeInput(
            @PathVariable("empNo") Integer empNo,
            @ModelAttribute("employeeForm") EmployeeForm form,
            Model model) {

        if (form.getLastName() == null) {
            Employee employee = employeeService.findById(empNo);
            if (employee != null) {
                form.setEmpNo(employee.getEmpNo());
                form.setLastName(employee.getLastName());
                form.setFirstName(employee.getFirstName());
                form.setLastKana(employee.getLastKana());
                form.setFirstKana(employee.getFirstKana());
                form.setPassword(employee.getPassword());
                form.setGender(employee.getGender());
                form.setDeptNo(employee.getDepartment() != null ? employee.getDepartment().getDeptNo() : null);
            }
        }

        model.addAttribute("departments", departmentService.findAll());
        return "change";
    }

    @PostMapping("/changeConfirm")
    public String confirmChange(
            @Validated @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.findAll());
            return "change";
        }
        Department department = departmentService.findById(form.getDeptNo());
        model.addAttribute("department", department);
        return "change_confirm";
    }

    @PostMapping("/changeEmployee")
    public String changeEmployee(@ModelAttribute("employeeForm") EmployeeForm form, Model model) {
        Employee employee = employeeService.update(form);
        model.addAttribute("employee", employee);
        return "change_complete";
    }
}
