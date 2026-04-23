package jp.co.trainocate.eims;

/**
 * Spring Boot アプリケーションのエントリーポイントとなるクラスです。
 * <p>
 * main メソッドからアプリケーションを起動します。
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EimsSpringBootApplication {

    /**
     * アプリケーションを起動するメインメソッド。
     * <p>
     * {@link SpringApplication#run(Class, String...)} を呼び出すことで
     * Spring Boot が提供する組み込みサーバー(Tomcat)が起動し、
     * すべてのコンポーネントが自動的に初期化されます。
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
            // Spring Boot を起動し、Web アプリケーション全体を立ち上げる
            SpringApplication.run(EimsSpringBootApplication.class, args);
    }

}
