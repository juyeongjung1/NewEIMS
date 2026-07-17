package jp.co.trainocate.eims.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
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

    // ===== 追加機能③ 退職者管理（設計書03 §10 テスト観点・受け入れ条件）=====

    @Test
    @DisplayName("論理削除すると在籍者の削除フラグが1になる")
    void logicalDeleteSetsDeleteFlagToOne() {
        Employee active = new Employee();
        active.setEmpNo(10001);
        active.setDeleteFlg(0);
        when(employeeRepository.findById(10001)).thenReturn(Optional.of(active));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.deleteById(10001);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getDeleteFlg());
    }

    @Test
    @DisplayName("復元すると退職者の削除フラグが0になる")
    void restoreSetsDeleteFlagToZero() {
        Employee retiree = new Employee();
        retiree.setEmpNo(10019);
        retiree.setDeleteFlg(1);
        when(employeeRepository.findById(10019)).thenReturn(Optional.of(retiree));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.restoreById(10019);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getDeleteFlg());
    }

    @Test
    @DisplayName("完全削除すると対象レコードが物理削除される")
    void physicalDeleteRemovesRecord() {
        employeeService.physicalDeleteById(10019);

        verify(employeeRepository).deleteById(10019);
    }

    @Test
    @DisplayName("存在しない社員番号の論理削除は保存を行わず異常も起きない")
    void logicalDeleteOnMissingEmployeeDoesNothing() {
        when(employeeRepository.findById(99999)).thenReturn(Optional.empty());

        employeeService.deleteById(99999);

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("存在しない社員番号の復元は保存を行わず異常も起きない")
    void restoreOnMissingEmployeeDoesNothing() {
        when(employeeRepository.findById(99999)).thenReturn(Optional.empty());

        employeeService.restoreById(99999);

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("氏名の通常検索は在籍者のみ（delete_flg=0）を対象にする")
    void nameSearchExcludesRetirees() {
        when(employeeRepository.findByDeleteFlgAndLastNameContainingOrDeleteFlgAndFirstNameContaining(
                0, "山", 0, "山")).thenReturn(List.of());

        employeeService.findByEmpName("山");

        verify(employeeRepository).findByDeleteFlgAndLastNameContainingOrDeleteFlgAndFirstNameContaining(
                0, "山", 0, "山");
    }

    @Test
    @DisplayName("部署の通常検索は在籍者のみ（delete_flg=0）を対象にする")
    void deptSearchExcludesRetirees() {
        when(employeeRepository.findByDeptNoAndDeleteFlg(100, 0)).thenReturn(List.of());

        employeeService.findByDeptNo(100);

        verify(employeeRepository).findByDeptNoAndDeleteFlg(100, 0);
    }

    @Test
    @DisplayName("退職者一覧は退職者のみ（delete_flg=1）を対象にする")
    void retireeListTargetsRetireesOnly() {
        when(employeeRepository.findByDeleteFlg(1)).thenReturn(List.of());

        employeeService.findRetirees();

        verify(employeeRepository).findByDeleteFlg(1);
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
