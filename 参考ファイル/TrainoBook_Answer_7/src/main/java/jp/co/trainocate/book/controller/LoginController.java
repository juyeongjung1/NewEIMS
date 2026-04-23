package jp.co.trainocate.book.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jp.co.trainocate.book.entity.User;
import jp.co.trainocate.book.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * ログイン（トップ画面）の作成と画面遷移、認証処理を担当するController。
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

	/**
	 * 【課題4.3追加】DBでユーザー情報を検索するためのRepository
	 */
	private final UserRepository userRepository;

	/**
	 * URL「/」にアクセスした際、ログイン画面（index.html）を返す。
	 */
	@GetMapping("/")
	public String index() {
		return "index";
	}

	/**
	 * ログインフォームからPOSTされたユーザIDとパスワードを受け取る。
	 * 
	 * 【補足】セキュリティの観点からは本来バリデーション（入力チェック）が必要ですが、
	 * 演習時間の都合上、本講座では書籍の登録・更新画面のみに実装し、ここでは省略しています。
	 * 
	 * @param userId   ユーザID（name属性: userId）
	 * @param password パスワード（name属性: password）
	 * @param session  セッション情報を操作するためのオブジェクト
	 * @param model    エラーメッセージ等を画面へ渡すためのModel
	 * @return ログイン成功時は書籍管理メニュー、失敗時はログイン画面
	 */
	@PostMapping("/login")
	public String login(Integer userId, String password, HttpSession session, Model model) {
		/*
		// ==============================================================
		// 【課題2.1の解答（モック）】第2章では以下の通り認証せずに遷移していました
		// ==============================================================
		// return "book_index";
		*/

		// ==============================================================
		// 【課題4.3の解答】DBでの認証処理と HttpSession によるセッション管理
		// ==============================================================
		
		// 1. DBから対象のユーザIDとパスワードで検索
		User user = userRepository.findByUserIdAndPassword(userId, password);

		// 2. ユーザが存在するかチェック（存在すればパスワードも一致している）
		if (user != null) {
			// 一致した場合：ログイン成功
			// HttpSessionに「userName」というキーでユーザー名を保存（全画面で引き継がれる）
			session.setAttribute("userName", user.getUserName());
			return "book_index";
		} else {
			// 一致しない（or ユーザーがいない）場合：ログイン失敗
			// エラーメッセージをModelに詰めて、ログイン画面（index.html）を再表示
			model.addAttribute("error", "ユーザーIDまたはパスワードが違います");
			return "index";
		}
	}

	/**
	 * 【課題4.3追加】ログアウト処理
	 * セッションを破棄して、ログイン画面へ戻る。
	 */
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // セッションのクリア
		return "redirect:/"; // トップへリダイレクト
	}
}
