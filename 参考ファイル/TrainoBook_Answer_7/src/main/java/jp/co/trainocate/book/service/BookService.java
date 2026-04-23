package jp.co.trainocate.book.service;

import java.util.List;
import jp.co.trainocate.book.entity.Book;
import jp.co.trainocate.book.form.BookForm;

/**
 * 【課題3.3】Bookエンティティに関するService層（インターフェース）。
 * 
 * Controller層とRepository層の間に立ち、データアクセスとビジネスロジックを仲介します。
 */
public interface BookService {
    
    /** 全件取得 */
    List<Book> findAllBooks();
    
    /** IDから1件取得 */
    Book findBookById(Integer id);
    
    /** タイトルによる部分一致検索 */
    List<Book> findBooksByTitle(String title);
    
    /** 価格帯による検索 */
    List<Book> findBooksByPrice(Integer minPrice, Integer maxPrice);

    /**
     * 【課題6.3追加】ジャンル名の一部一致と、指定価格以下による複合検索。
     * @param genreName ジャンル名（部分一致）
     * @param maxPrice 上限価格
     * @return 条件に合致する書籍リスト
     */
    List<Book> findBooksByComplexCondition(String genreName, Integer maxPrice);
    
    /**
     * 【課題3.6, 3.7】書籍の登録・更新
     * Formから受け取ったデータをEntityに詰め替えて保存します。
     * IDが存在する場合は更新（UPDATE）、存在しない場合は新規登録（INSERT）となります。
     */
    Book saveBook(BookForm bookForm);
    
    /** 削除の実行 */
    void deleteBook(Integer id);
}
