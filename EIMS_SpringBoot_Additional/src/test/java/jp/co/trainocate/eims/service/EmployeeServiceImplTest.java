package jp.co.trainocate.eims.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jp.co.trainocate.eims.entity.Employee;
import jp.co.trainocate.eims.form.EmployeeForm;
import jp.co.trainocate.eims.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void newEmployeeIsAlwaysSavedAsGeneralAndActive() {
        EmployeeForm form = employeeForm(null);
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.save(form);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getRole());
        assertEquals(0, captor.getValue().getDeleteFlg());
    }

    @Test
    void findActiveEmployeesUsesDeleteFlagZero() {
        when(employeeRepository.findByDeleteFlg(0)).thenReturn(List.of());

        employeeService.findActiveEmployees();

        verify(employeeRepository).findByDeleteFlg(0);
    }

    @Test
    void updateKeepsRoleAndDeleteFlagStoredInDatabase() {
        Employee existing = new Employee();
        existing.setEmpNo(10001);
        existing.setRole(1);
        existing.setDeleteFlg(1);
        when(employeeRepository.findById(10001)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee updated = employeeService.update(employeeForm(10001));

        assertEquals(1, updated.getRole());
        assertEquals(1, updated.getDeleteFlg());
    }

    private EmployeeForm employeeForm(Integer empNo) {
        EmployeeForm form = new EmployeeForm();
        form.setEmpNo(empNo);
        form.setLastName("山田");
        form.setFirstName("太郎");
        form.setLastKana("ヤマダ");
        form.setFirstKana("タロウ");
        form.setPassword("password");
        form.setGender(1);
        form.setDeptNo(100);
        return form;
    }
}
