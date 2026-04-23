package jp.co.trainocate.book.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jp.co.trainocate.book.entity.Book;

/**
 * 【課題3.2】Bookエンティティ用のリポジトリ。
 * 
 * 基本的なCRUD操作に加えて、書籍の検索に必要な独自メソッドを定義します。
 */
public interface BookRepository extends JpaRepository<Book, Integer> {
    
    /**
     * タイトルに特定の文字列を含む書籍を検索します（LIKE検索）。
     * 命名規則: findBy + Title + Containing
     * 
     * @param title 検索キーワード
     * @return 該当する書籍のリスト
     */
    List<Book> findByTitleContaining(String title);
    
    /**
     * 価格が指定された範囲内に収まる書籍を検索します（BETWEEN検索）。
     * 命名規則: findBy + Price + Between
     * 
     * @param minPrice 最低価格
     * @param maxPrice 最高価格
     * @return 該当する書籍のリスト
     */
    List<Book> findByPriceBetween(Integer minPrice, Integer maxPrice);

    /**
     * 【課題6.3追加】ジャンル名の一部一致（LIKE）かつ、指定価格以下の書籍を検索するJPQL。
     * 
     * 単純なメソッド名では表現しきれない複雑な結合や条件をJPQLで定義します。
     * @param genreName ジャンル名の一部（例：「プロ」）
     * @param maxPrice 上限価格
     * @return 条件を満たす書籍のリスト
     */
    @Query("SELECT b FROM Book b JOIN b.genre g WHERE g.name LIKE %:genreName% AND b.price <= :maxPrice")
    List<Book> findByGenreNameContainingAndMaxPrice(@Param("genreName") String genreName, @Param("maxPrice") Integer maxPrice);
}
