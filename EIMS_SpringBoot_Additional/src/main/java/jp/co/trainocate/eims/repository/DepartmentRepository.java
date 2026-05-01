package jp.co.trainocate.eims.repository;

/**
 * 部署エンティティ用のリポジトリ。
 * JpaRepository を継承するだけで基本的な CRUD 操作が利用できます。
 */

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.trainocate.eims.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
