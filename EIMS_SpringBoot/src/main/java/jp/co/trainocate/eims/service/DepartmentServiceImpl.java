package jp.co.trainocate.eims.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.trainocate.eims.entity.Department;
import jp.co.trainocate.eims.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * DepartmentService の実装クラス。
 * Repository を利用して部署情報を取得します。
 */
public class DepartmentServiceImpl implements DepartmentService {

    /** DepartmentRepository のDI */
    @Autowired
    private final DepartmentRepository departmentRepository;

    /** 全部署を取得 */
    @Override
    public List<Department> findAll() {
        // Repository を利用して department テーブルを全件取得
        return departmentRepository.findAll();
    }

    /** 部署番号で部署を取得 */
    @Override
    public Department findById(Integer deptno) {
        // Optional の中身が無い場合は null を返す
        return departmentRepository.findById(deptno).orElse(null);
    }
}
