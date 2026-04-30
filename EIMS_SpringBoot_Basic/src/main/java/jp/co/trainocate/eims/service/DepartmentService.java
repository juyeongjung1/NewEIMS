package jp.co.trainocate.eims.service;

/**
 * 部署に関するビジネスロジックを定義するサービスインターフェース。
 */

import java.util.List;

import jp.co.trainocate.eims.entity.Department;

public interface DepartmentService {
    /**
     * 部署テーブルを全件取得します。
     * 画面のプルダウン表示などに使用します。
     * @return 部署エンティティのリスト
     */
    List<Department> findAll();

    /**
     * 主キーである部署番号を指定して部署情報を取得します。
     * @param deptNo 部署番号
     * @return 部署エンティティ。存在しない場合は null
     */
    Department findById(Integer deptNo);
}
