package jp.co.trainocate.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 【課題3.1】bookテーブルに対応するEntityクラス。
 * 
 * データベースの「book」テーブルの1レコードを表します。
 * JPAを通じてデータの読み書きが行われます。
 */
@Entity
@Data
@Table(name = "book")
public class Book {
    
    /** 書籍ID (主キー) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 書籍タイトル */
    @Column(length = 255, nullable = false)
    private String title;

    /** 著者名 */
    @Column(length = 100, nullable = false)
    private String author;

    /** 価格 */
    @Column(nullable = false)
    private Integer price;

    /**
     * 【6章の内容】
     * Genreエンティティとのリレーション（多対1）。
     * 同じ genre_id カラムを使用するため、エラーを防ぐ目的で
     * insertable = false, updatable = false を設定し「読み取り専用」としています。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", insertable = false, updatable = false)
    private Genre genre;

    /**
     * 【6章の内容】
     * 登録・更新時に利用するジャンルID（数値）。
     * Genreオブジェクト側を読み取り専用にしているため、データ更新はこちらの数値で行います。
     */
    @Column(name = "genre_id")
    private Integer genreId;
}
