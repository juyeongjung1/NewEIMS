package jp.co.trainocate.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import jp.co.trainocate.book.entity.Genre;

/**
 * 【課題3.2】Genreエンティティ用のリポジトリ。
 * 
 * 基本的なCRUD操作はJpaRepositoryによって自動提供されます。
 */
public interface GenreRepository extends JpaRepository<Genre, Integer> {
}
