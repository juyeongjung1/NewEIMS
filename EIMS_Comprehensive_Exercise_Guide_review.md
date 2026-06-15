# EIMS 総合演習ガイドと EIMS_SpringBoot_Basic のレビュー結果

レビュー日: 2026-06-15

## 対象

- `EIMS_Comprehensive_Exercise_Guide.md`
- `EIMS_SpringBoot_Basic/src/main`
- `EIMS_SpringBoot_Basic/src/test`
- 関連確認: `DBSET/EIMS_DBSET`

## 確認結果の要約

`EIMS_SpringBoot_Basic` は、登録・変更の確認画面、部署プルダウン、Bean Validation など、仕様書の標準実装にかなり近い構成です。  
ただし、仕様書と実装の間には、画面文言・画面項目・入力チェック・クラス図/URL表・DB定義で複数の不整合があります。

`EIMS_SpringBoot_Basic` で `mvnw.cmd test` を実行し、17件すべて成功しました。

## 指摘事項

### 1. DB接続パスワードがDB構築ファイルとアプリ設定で一致しない

- アプリ設定: `EIMS_SpringBoot_Basic/src/main/resources/application.properties:5-6`
  - `spring.datasource.username=eimsuser`
  - `spring.datasource.password=eimspass`
- DBユーザー作成: `DBSET/EIMS_DBSET/createUser.sql:3-7`
  - ユーザーは `eimsuser`
  - パスワードは `Pa$$w0rd`

DB構築ファイルどおりに環境構築した場合、BasicアプリはMySQLへ接続できません。  
どちらを正とするか決め、`application.properties` または `createUser.sql` を合わせる必要があります。

### 2. サーバー側入力チェックが仕様書より弱い

仕様書では、登録・変更で以下を必須または制約ありとしています。

- 部署は必須: `EIMS_Comprehensive_Exercise_Guide.md:181`, `EIMS_Comprehensive_Exercise_Guide.md:236`, `EIMS_Comprehensive_Exercise_Guide.md:869`
- パスワードは `4～16文字` かつ `半角英数字`: `EIMS_Comprehensive_Exercise_Guide.md:867`
- 性別はラジオボタンの `1:男, 2:女`: `EIMS_Comprehensive_Exercise_Guide.md:868`

実装では `EmployeeForm` に以下の不足があります。

- `deptNo` に `@NotNull` がない: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/form/EmployeeForm.java:47-48`
- `password` に半角英数字チェックがない: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/form/EmployeeForm.java:39-40`
- `gender` は `@NotNull` のみで、1/2以外を拒否しない: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/form/EmployeeForm.java:43-45`

HTMLには `required` がありますが、直接POSTされた場合は回避できます。仕様書を実装に合わせるか、Formに `@NotNull`、`@Pattern`、`@Min/@Max` などを追加する必要があります。

### 3. 社員一覧の表示順「社員番号昇順」が実装で保証されていない

仕様書は社員一覧を `emp_no` 昇順としています。

- `EIMS_Comprehensive_Exercise_Guide.md:112-114`

実装は `JpaRepository#findAll()` をそのまま使っています。

- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:37-40`
- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/service/EmployeeServiceImpl.java:18-20`

DBの現在の返却順に見かけ上依存しており、仕様としては不安定です。`findAll(Sort.by("empNo"))` などで明示するのが安全です。

### 4. 検索入力不正時に検索画面へ戻るが、部署リストを再設定していない

仕様書は、検索キーワードが `null` または空文字の場合、検索画面に留まることを求めています。

- `EIMS_Comprehensive_Exercise_Guide.md:129-130`

実装は `search` を返しますが、`departments` を再設定しません。

- 社員番号: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:51-55`
- 氏名: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:68-72`
- 部署: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:79-83`

`search.html` は部署プルダウンで `departments` を参照します。直接リクエストやHTMLバリデーション回避時に、部署候補が消える可能性があります。

### 5. 削除対象が存在しない場合の仕様が未実装

仕様書は、削除対象が存在しない場合に削除できない旨を表示するとしています。

- `EIMS_Comprehensive_Exercise_Guide.md:281-283`

実装では、削除確認画面で `employeeService.findById()` の結果をそのままModelに入れ、null時の分岐がありません。

- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:125-129`

削除確定も `deleteById(empNo)` を直接呼びます。

- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:133-136`

存在しない社員番号が指定された場合の画面表示・遷移・エラーメッセージ仕様を決める必要があります。

### 6. 完了画面のボタン仕様が実装と一致しない

仕様書では以下のボタンが必要です。

- 登録完了: `続けて登録する` と `メニューに戻る`: `EIMS_Comprehensive_Exercise_Guide.md:198-201`
- 変更完了: `メニューに戻る` と `検索画面に戻る`: `EIMS_Comprehensive_Exercise_Guide.md:255-257`
- 削除完了: `メニューに戻る` と `検索画面に戻る`: `EIMS_Comprehensive_Exercise_Guide.md:292-294`

実装では、各完了画面に `メニューに戻る` だけがあります。

- `EIMS_SpringBoot_Basic/src/main/resources/templates/input_complete.html:16-18`
- `EIMS_SpringBoot_Basic/src/main/resources/templates/change_complete.html:16-18`
- `EIMS_SpringBoot_Basic/src/main/resources/templates/delete_complete.html:16-18`

UIフロー図でも続けて登録・検索画面へ戻る導線が書かれているため、実装不足か仕様過多のどちらかです。

### 7. 確認画面の表示形式とパスワードマスクが仕様書と異なる

仕様書は、登録確認・変更確認で氏名とカナを半角スペースで連結し、パスワードを `●●●●●●●●` 固定表示にするとしています。

- 登録確認: `EIMS_Comprehensive_Exercise_Guide.md:192-197`
- 変更確認: `EIMS_Comprehensive_Exercise_Guide.md:249-254`

実装は氏・名・カナを別行で表示し、パスワードは `********（非表示）` です。

- `EIMS_SpringBoot_Basic/src/main/resources/templates/input_confirm.html:20-26`
- `EIMS_SpringBoot_Basic/src/main/resources/templates/change_confirm.html:20-27`

機能上は大きな問題ではありませんが、教材の画面仕様としては統一が必要です。

### 8. 社員詳細画面・削除確認画面の表示項目が仕様書と異なる

仕様書は、社員詳細画面を一覧画面と同じ4項目としています。

- `EIMS_Comprehensive_Exercise_Guide.md:144-146`

実装は社員番号、氏、名、氏カナ、名カナ、性別、所属部署の7行表示です。

- `EIMS_SpringBoot_Basic/src/main/resources/templates/employee_detail.html:19-25`

また、削除確認画面は仕様書では `社員番号、氏名+カナ、性別、部署名` を表示するとしています。

- `EIMS_Comprehensive_Exercise_Guide.md:288-291`

実装は社員番号、氏、名、所属部署のみで、カナと性別がありません。

- `EIMS_SpringBoot_Basic/src/main/resources/templates/delete_confirm.html:20-23`

詳細画面は実装のほうが情報量が多い一方、削除確認画面は仕様より情報が少ないです。

### 9. 0件時メッセージが仕様書内で統一されていない

仕様書内で、検索0件時メッセージが複数あります。

- `該当する社員は存在しませんでした`: `EIMS_Comprehensive_Exercise_Guide.md:142`
- `該当する社員が存在しないことを...表示`: `EIMS_Comprehensive_Exercise_Guide.md:476`
- `一致する社員は見つかりませんでした`: `EIMS_Comprehensive_Exercise_Guide.md:1864`, `EIMS_Comprehensive_Exercise_Guide.md:1866`
- `検索条件に一致する社員は見つかりませんでした。`: `EIMS_Comprehensive_Exercise_Guide.md:1885`

実装は共通メッセージ一覧と同じ `検索条件に一致する社員は見つかりませんでした。` です。

- `EIMS_SpringBoot_Basic/src/main/resources/templates/search_result.html:17-18`

仕様書側を共通メッセージ一覧に合わせるのが自然です。

### 10. URLマッピング表と実Controllerメソッド名が一致しない

仕様書のURLマッピング表では、検索画面や検索処理のメソッド名が実装と違います。

- `/search` のメソッドが `index`: `EIMS_Comprehensive_Exercise_Guide.md:1532`
- `/selectByEmpName`、`/selectByDeptNo`、`/selectByEmpNo` のメソッドがすべて `search`: `EIMS_Comprehensive_Exercise_Guide.md:1533-1535`

実装は以下です。

- `/search` は `showSearchPage`: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:44-47`
- `/selectByEmpNo` は `selectByEmpNo`: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:51-64`
- `/selectByEmpName` は `selectByEmpName`: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:68-75`
- `/selectByDeptNo` は `selectByDeptNo`: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:79-86`

URL自体は一致していますが、設計書としてはメソッド名を実装に合わせる必要があります。

### 11. クラス図に実装済みでない `update()` と存在しない依存がある

仕様書のクラス図では、変更機能に `update(EmployeeForm)` が定義されています。

- `EIMS_Comprehensive_Exercise_Guide.md:1349-1356`
- `EIMS_Comprehensive_Exercise_Guide.md:1437-1445`

実装では `update()` は存在せず、変更確定も `save(EmployeeForm)` を使っています。

- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java:171-175`
- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/service/EmployeeService.java:47-58`

また、全体クラス図では `EmployeeServiceImpl` が `DepartmentRepository` に依存するとあります。

- `EIMS_Comprehensive_Exercise_Guide.md:1447-1450`
- `EIMS_Comprehensive_Exercise_Guide.md:1507-1508`

実装の `EmployeeServiceImpl` は `EmployeeRepository` のみを保持しています。

- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/service/EmployeeServiceImpl.java:14-17`

設計書を実装に合わせるなら `update()` と `DepartmentRepository` 依存を削除し、変更処理は `save()` に統一するのが最小修正です。

### 12. テスト仕様が機能全体を網羅していない

仕様書11章のControllerテストは13件のみです。

- `EIMS_Comprehensive_Exercise_Guide.md:1857-1873`

実装テストもこの13件にほぼ対応しており、`mvnw.cmd test` は17件成功しました。  
ただし、機能仕様と比べると以下が未定義または未検証です。

- `/employeeList`
- `/selectByDeptNo`
- `/changeConfirm`
- `/changeEmployee`
- `/deleteEmployee`
- 検索条件未入力時の挙動
- 削除対象が存在しない場合
- 登録/変更の部署未選択、性別1/2以外、パスワード半角英数字違反

テスト章の「網羅する」という表現に対して、実際のケース数は不足しています。

### 13. Repositoryテストが本番用DB設定に依存している

Repositoryテストは `@AutoConfigureTestDatabase(replace = Replace.NONE)` を使い、アプリ設定のMySQLに接続します。

- `EIMS_SpringBoot_Basic/src/test/java/jp/co/trainocate/eims/repository/EmployeeRepositoryTest.java:21-22`
- `EIMS_SpringBoot_Basic/src/main/resources/application.properties:4-9`

`src/test/resources` にはテスト用プロパティがありません。  
DB構築ファイルには `eimsdb_test` 用のセットがありますが、Basicプロジェクトのテストはそれを使っていません。

受講者環境でDBの有無やユーザー設定が違うとテストが落ちる可能性があります。

### 14. DB仕様書とDB構築ファイルが一致しない

仕様書6章では以下です。

- `department.dept_name` は `VARCHAR(10)`: `EIMS_Comprehensive_Exercise_Guide.md:908-912`
- `employee.dept_no` は `NOT NULL`: `EIMS_Comprehensive_Exercise_Guide.md:914-924`
- 初期データは `employee 60件`: `EIMS_Comprehensive_Exercise_Guide.md:930-935`
- 部署は `総務部(400)`、`企画部(600)`: `EIMS_Comprehensive_Exercise_Guide.md:934`

DB構築ファイルでは以下です。

- `department.dept_name` は `VARCHAR(20)`: `DBSET/EIMS_DBSET/createDB.sql:7-10`
- `employee.dept_no` は nullable: `DBSET/EIMS_DBSET/createDB.sql:14-28`
- `role`、`delete_flg` が追加されている: `DBSET/EIMS_DBSET/createDB.sql:21-24`
- 初期社員データは20件のみ: `DBSET/EIMS_DBSET/createDB.sql:41-61`
- `企画部(400)`、`総務部(600)` になっている: `DBSET/EIMS_DBSET/createDB.sql:32-38`

BasicソースのEntityは、部署名長は仕様書寄りですが、`Employee.deptNo` は `nullable = false` がなくDB構築ファイル寄りです。

- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/entity/Department.java:27-29`
- `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/entity/Employee.java:53-55`

仕様書、Basicソース、DB構築ファイルのどれを正とするか整理が必要です。

### 15. ユースケース仕様書の代替フロー参照に抜けがある

UC001の基本フローは `alt3-1, alt3-2` 参照と書いていますが、代替フローには `alt3-3` もあります。

- `EIMS_Comprehensive_Exercise_Guide.md:439`
- `EIMS_Comprehensive_Exercise_Guide.md:441-444`

UC002の基本フロー5は `alt5-1, alt5-2` 参照と書いていますが、代替フローには `alt5-3` もあります。

- `EIMS_Comprehensive_Exercise_Guide.md:473`
- `EIMS_Comprehensive_Exercise_Guide.md:478-480`

誤字というより参照漏れです。仕様書の読み手が見落としやすい箇所です。

## 優先して直すなら

1. DB接続パスワードの不一致を解消する。
2. 入力チェック仕様を実装に合わせるか、FormのValidationを強化する。
3. 完了画面の不足ボタンと削除対象なしの扱いを決める。
4. URLマッピング表・クラス図・メッセージ表を実装名に合わせて修正する。
5. テスト仕様に未検証の主要エンドポイントを追加する。

