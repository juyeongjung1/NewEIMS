package jp.co.trainocate.book.service;

import java.util.List;
import jp.co.trainocate.book.entity.Genre;

/**
 * 【課題6.2追加】ジャンルに関連するビジネスロジックを定義するインターフェース。
 */
public interface GenreService {
    /**
     * すべてのジャンルを取得します。
     * @return ジャンルのリスト
     */
    List<Genre> findAllGenres();
}
