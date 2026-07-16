package jp.co.trainocate.eims.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.service.EmployeeService;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

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
}
