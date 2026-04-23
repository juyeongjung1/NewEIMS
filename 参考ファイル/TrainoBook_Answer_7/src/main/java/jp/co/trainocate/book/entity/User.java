package jp.co.trainocate.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 【課題4.2】userテーブルに対応するEntityクラス。
 * 
 * ログイン認証に必要なユーザー情報を管理するためのクラスです。
 */
@Entity
@Data
@Table(name = "user")
public class User {

    /** ユーザーID (主キー) */
    @Id
    @Column(name = "user_id")
    private Integer userId;

    /** パスワード */
    @Column(length = 255, nullable = false)
    private String password;

    /** ユーザー名 */
    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;
}
