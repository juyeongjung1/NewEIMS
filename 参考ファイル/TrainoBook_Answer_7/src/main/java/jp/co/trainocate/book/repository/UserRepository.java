package jp.co.trainocate.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import jp.co.trainocate.book.entity.User;

/**
 * 【課題4.2】Userエンティティ用のリポジトリ。
 * 
 * 基本的なCRUD操作はJpaRepositoryによって自動提供されます。
 * 主キーが Integer (userId) なので、第2型引数は Integer となります。
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * 【課題4.2】ユーザIDとパスワードが一致するユーザーを検索する
     * @param userId ユーザID
     * @param password パスワード
     * @return 条件に一致するUser（存在しない場合はnullなど）
     */
    User findByUserIdAndPassword(Integer userId, String password);
}
