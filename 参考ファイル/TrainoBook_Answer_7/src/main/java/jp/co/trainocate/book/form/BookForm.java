package jp.co.trainocate.book.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 書籍登録用・更新用のデータを受け取るためのFormクラス。
 * 
 * フォームの各入力項目（name属性）と一致させることでSpring MVCがデータをバインドします。
 */
@Data
public class BookForm {

	// ==========================================
	// 【課題3.7追加分】 書籍ID（更新用）
	// ==========================================
	private Integer id;

	// ==========================================
	// 【課題2.3実装分】 新規登録用フィールド
	// ==========================================
	/** 書籍タイトル（name属性: title） */
	@NotBlank(message = "書籍名を入力してください")
	private String title;

	/** 著者名（name属性: author） */
	@NotBlank(message = "著者名を入力してください")
	private String author;

	/** 価格（name属性: price） */
	@NotNull(message = "価格を入力してください")
	@Min(value = 0, message = "価格は0以上で入力してください")
	private Integer price;

	// ==========================================
	// 【課題3.6追加分】 ジャンルID追加
	// ==========================================
	@NotNull(message = "ジャンルIDを入力してください")
	private Integer genreId;
}
