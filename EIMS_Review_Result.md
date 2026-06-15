# EIMS 総合演習ガイド 追加レビュー結果

レビュー対象: `EIMS_Comprehensive_Exercise_Guide.md` および `EIMS_SpringBoot_Basic`
レビュー日: 2026-06-15

`EIMS_Comprehensive_Exercise_Guide_review.md` に記載されている「あえて修正しないようにした内容」を考慮し、仕様書とソースコードの比較・分析を徹底的に行いました。

## 1. 既存のレビュー指摘事項に関する重要な事実（アップデート）

事前に連携いただいた `EIMS_Comprehensive_Exercise_Guide_review.md` に記載されている指摘事項の多くは、**現在のソースコードおよび仕様書では既に修正・解消されている**ことが確認できました。
「あえて修正していない」と認識されているかもしれませんが、実態としては以下の項目は既に正しく実装（または仕様書修正）されています。

- **完了画面のボタン不足（指摘事項6）**: 現在の `input_complete.html`、`change_complete.html`、`delete_complete.html` には、すべて仕様書通りの2つのボタン（「続けて登録する」「検索画面に戻る」など）が存在しています。
- **削除確認画面の項目不足（指摘事項8）**: 現在の `delete_confirm.html` には、指摘されていた「カナ」や「性別」を含め、仕様書通りの全7項目が正しく表示されています。
- **0件時メッセージの不統一（指摘事項9）**: 仕様書の該当箇所（142行目など）は、既に「検索条件に一致する社員は見つかりませんでした。」に統一修正されています。
- **URLマッピングとメソッド名の不一致（指摘事項10）**: 仕様書のマッピング表と、`EmployeeController` の `index` メソッドおよび `search` メソッド名は完全に一致しています。
- **`update()` の未実装と存在しない依存（指摘事項11）**: `EmployeeService` および `EmployeeServiceImpl` には `update()` が実装されており、コントローラーから呼ばれています。また仕様書のクラス図上も `EmployeeServiceImpl` が `DepartmentRepository` に依存するような記載はありません。

---

## 2. 新たに発見された仕様書とソースコードの不整合・誤り

既存のレビューシートに記載されていない、新たな不整合・誤字・バグを以下の通り報告します。

### ① コントローラー内の不要なコード重複（ソースコードの誤り）
`EmployeeController.java` の `search` メソッド内（社員番号で検索し、ヒットしなかった場合の処理）において、全く同じコードが2行重複して記述されています。
- **箇所**: `EIMS_SpringBoot_Basic/src/main/java/jp/co/trainocate/eims/controller/EmployeeController.java` (61-62行目)
- **内容**:
  ```java
  model.addAttribute("employees", new ArrayList<Employee>());
  model.addAttribute("employees", new ArrayList<Employee>());
  ```

### ② 仕様書にないメソッドの定義と不自然な戻り値（ソースコードの誤り）
`EmployeeRepository` と `EmployeeService` において、主キー（社員番号）で検索するメソッドが定義されていますが、戻り値が `List` になっており、かつ仕様書のクラス図に存在しません。
- **箇所**: `EmployeeRepository.java` (19行目), `EmployeeService.java` (24行目)
- **内容**: `List<Employee> findByEmpNo(Integer empNo);`
- **問題点**:
  1. 仕様書の設計クラス図（9.1章や9.2章など）には、このメソッドは定義されていません。
  2. `empNo` は主キー（`@Id`）であるため、戻り値は `Optional<Employee>` または単一の `Employee` であるべきですが、`List<Employee>` が返る設計になっています。
  3. 実際の `EmployeeController` はこのメソッドを使用しておらず、仕様書通りに標準の `findById(empNo)` を使用しています。そのため、このメソッド自体が不要（デッドコード）です。

### ③ 共通メッセージ `V001` が画面に表示されない（仕様と実装の不整合）
仕様書の12章（共通メッセージ一覧）において、バリデーションエラー時のメッセージとして `V001: 入力内容に不備があります。` が定義されていますが、実装上どこにも使われていません。
- **箇所**: `EIMS_Comprehensive_Exercise_Guide.md` (1893行目), `input.html`, `change.html`
- **問題点**: 実際の登録画面・変更画面では、項目個別のエラー（`th:errors`）は表示されますが、画面上部などに `V001` のメッセージを表示する実装が漏れています。

### ④ コンポーネントテスト仕様書の URL パスの誤字（仕様書の誤字）
仕様書の11.2章「Controller テストケース」の表において、削除確認と変更画面のパスパラメータが間違っています。
- **箇所**: `EIMS_Comprehensive_Exercise_Guide.md` (1873行目, 1876行目)
- **内容**:
  - `CT-C-012`: `GET /deleteConfirm/{id}` と記載されていますが、正しくは `GET /deleteConfirm/{empNo}` です。
  - `CT-C-014`: `GET /changeInput/{id}` と記載されていますが、正しくは `GET /changeInput/{empNo}` です。
- **理由**: 10.1章の「URLマッピング一覧」や実際のコントローラーの `@GetMapping` の定義では `{empNo}` が正しいパス変数となっています。

### ⑤ 変更画面への GET アクセス時の判定ロジックが脆弱（実装上の懸念）
`EmployeeController.java` の `changeInput` メソッドにおいて、初回アクセスかバリデーションエラー後の戻りかどうかの判定を `if (employeeForm.getLastName() == null)` で行っています。
- **箇所**: `EmployeeController.java` (146行目)
- **懸念点**: 動作上は問題ありませんが、URL のパス変数 `{empNo}` に存在しない社員番号を手入力された場合、`employeeService.findById(empNo)` が `null` を返し、次の行で `NullPointerException` が発生します。削除機能（`deleteConfirm`）では `null` チェックが行われているため、変更機能でも同様のチェックを実装するか、仕様として割り切るかの検討が必要です。
