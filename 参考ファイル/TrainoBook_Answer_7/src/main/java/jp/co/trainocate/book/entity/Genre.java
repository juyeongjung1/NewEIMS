package jp.co.trainocate.book.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 【課題3.1】genreテーブルに対応するEntityクラス。
 * 
 * ジャンル情報を管理するためのクラスです。
 */
@Entity
@Data
@Table(name = "genre")
public class Genre {
    
    /** ジャンルID (主キー) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** ジャンル名 */
    @Column(length = 50, nullable = false)
    private String name;

    /**
     * 【6章の内容】
     * Bookエンティティとのリレーション（1対多）。
     * 1つのジャンルに対して、複数の書籍が紐づく関係性を定義します。
     */
    @OneToMany(mappedBy = "genre")
    private List<Book> bookList;
}
