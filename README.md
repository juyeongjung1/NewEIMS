# NewEIMS

社員情報管理システム EIMS の演習教材、講師用解答、テスト、資料作成用ファイルを管理するリポジトリです。
このページは、何を読む・編集する・配布するためのファイルなのかを案内します。

**今回の指摘に対する判断は [指摘事項の検証結果](docs/指摘事項の検証結果.md) にまとめています。**
2026年11月4日の指摘に対応し、仕様原稿・解答コード・画面画像・配布PDF・ZIPを更新しました。追加確認では承認済みの退職確認項目と仕様表記を揃え、同一内容の別名コピー3件を整理しています。さらに、配布SQLの正本・同期方法、共通機能テストの設定を統一し、DBSET内の `old` を削除しました。

## 最初に見る場所

| 目的 | 参照先 | 位置づけ |
| --- | --- | --- |
| 共通機能の仕様を読む・直す | [EIMS_Comprehensive_Exercise_Guide.md](EIMS_Comprehensive_Exercise_Guide.md) | 検索・一覧・登録・変更・削除の編集元。配布PDFと照合して使う |
| 追加機能の仕様を読む・直す | [追加機能設計書.md](追加機能の設計書/追加機能設計書.md) | ログイン、退職者管理、権限、画面改善の編集元 |
| 共通機能の解答を確認する | [EIMS_SpringBoot_Basic](EIMS_SpringBoot_Basic/) | 講師用の動作例・テストを含むMavenプロジェクト |
| 追加機能の解答を確認する | [EIMS_SpringBoot_Additional](EIMS_SpringBoot_Additional/) | 共通機能に追加機能を組み込んだ解答プロジェクト |
| 受講者向けの資料一式を確認する | `EIMS_受講者配布資料/` | スターター、共通・追加仕様PDF、テスト書式、予実管理表と一式ZIP |
| DBの構造・初期化方法を確認する | [DBSET](DBSET/) | 通常用とテスト用のSQL・実行バッチの正本 |
| テストコードの配布元を確認する | [EIMS_TestCode](EIMS_TestCode/) | コピーして利用するテスト解答とテスト用Entity。独立したアプリではない |

> 配布対象のZIPとPDFはGit管理しています。作業記録や旧版など、ローカルに存在する資料すべてをGit管理しているわけではありません。

## 用途別のファイル一覧

### 編集する仕様と画面素材

| ファイル・フォルダー | 内容 |
| --- | --- |
| `EIMS_Comprehensive_Exercise_Guide.md` | 共通機能の仕様。DB定義、クラス図、URL一覧、共通メッセージもここにある |
| `images/` | 共通機能ガイドが参照する画面画像とロゴ。原稿と一緒に管理する |
| `追加機能の設計書/追加機能設計書.md` | 追加機能の仕様。ログイン画面の登録ボタンは廃止。社員登録本体へのアクセス制限は発展課題として維持 |
| `追加機能の設計書/images/` | 追加機能設計書の画面画像 |
| `追加機能の設計書/build-additional-pdf.js` | 上記Markdownから `追加機能の設計書/output/EIMS_追加機能設計書.pdf` を作るスクリプト。今回の配布版はこのスクリプトで生成 |
| `EIMS_Comprehensive_Exercise_Guide_PDF_Page_Alignment_20260615.md` | 共通ガイドのページ・差分整合に関する補助資料。仕様本文とは別 |

### ソースコードとテスト

| ファイル・フォルダー | 内容・注意点 |
| --- | --- |
| `EIMS_SpringBoot_Basic/src/main/` | 共通機能のJavaコード、HTML、設定、静的ファイル |
| `EIMS_SpringBoot_Basic/src/test/` | 共通機能のJavaテスト |
| `EIMS_SpringBoot_Basic/playwright/` | ブラウザテストと実行画面。追加機能用のテストも混在している |
| `EIMS_SpringBoot_Basic/新しいフォルダー/` | 追加機能用Playwright一式と実行バッチ。既存 `playwright/` との統合要否を確認する対象 |
| `EIMS_SpringBoot_Additional/src/main/` | 追加機能を含むJavaコード・画面 |
| `EIMS_SpringBoot_Additional/src/test/` | 追加機能のJavaテスト |
| `EIMS_TestCode/controller/`、`repository/` | 受講者がコピーして利用するテスト解答。Basic内のテストと常に同一ではない |
| `EIMS_TestCode/entity/` | テストデータ作成用コンストラクターを含むEntity。テストクラスではなく `src/main/java` 側に置くもの |
| `DBSET/EIMS_DBSET/`、`DBSET/EIMS_TEST_DBSET/` | 通常DB・テストDBのSQL・バッチの正本。同種のファイルをスターターへ同期する。本番用とテスト用の初期データは用途別。内容確認だけでバッチを実行しない |
| 各プロジェクトの `target/` | ビルド生成物。編集するソースではない |

共通解答 `EIMS_SpringBoot_Basic` と配布テストコード `EIMS_TestCode` は、`@ActiveProfiles("test")` でテスト用設定を使います。Repositoryテストの実行前に `EIMS_TEST_DBSET` で `eimsdb_test` を構築し、`src/test/resources/application-test.properties` の接続先と `spring.jpa.hibernate.ddl-auto=validate` を確認してください。`validate` はテーブルを自動作成しません。配布テストコードのコピー先に設定ファイルがない場合は、スターターから同じパスへコピーします。

### 配布用の資料と圧縮ファイル

| ファイル・フォルダー | 内容・注意点 |
| --- | --- |
| `EIMS_受講者配布資料/EIMS_受講者配布資料.zip` | 受講者用の一式。更新PDFを含め、ZIP内の全ファイルと配布元のバイト一致を確認 |
| `EIMS_受講者配布資料/EIMS_StarterKits/` | 未完成のスタータープロジェクト。DBSETは `EIMS_SpringBoot/DBSET/` 内にあり、同種SQL・バッチを正本と同期する。任意課題用の `addEmployeesForPaging.sql` は維持 |
| `EIMS_StarterKits.zip` | 受講者一式のスターターと同じ内容・配置に統一。DBSETはアプリ内にある |
| `EIMS_SpringBoot_Additional(追加機能解答).zip` | 追加機能のソース配布版。現在のソース・テストから生成し、ビルド生成物は含めない |
| `EIMS_TestCode(解答).zip` | 更新済み `EIMS_TestCode/` と同期。パッケージ名は `jp.co.trainocate.eims` |
| `DBSET.zip` | `DBSET/` のSQL・バッチだけを同梱。混入していた追加機能ZIPは除外 |
| `テスト関連のテンプレート.zip` | テスト仕様書等の記入用書式 |
| `予実管理表.xlsx` | 作業の予定・実績を記入する書式。配布一式にも同名ファイルがある |
| `テストケース仕様書兼結果報告書(EIMS)_解答例/` | 講師用のテストケース解答Excel |

`各ファイルの説明.txt` も現行の配布先に合わせて更新しています。詳しい用途分類と再生成手順はこのREADMEを使ってください。
共通機能の解答ZIPは、今回リポジトリ直下の `EIMS_SpringBoot_Basic(解答).zip` として生成しています。親フォルダーの同名ZIPは旧版です。

### 現行の配布PDF

| 内容 | 配布用ファイル | 同期しているコピー |
| --- | --- | --- |
| 共通仕様 | [EIMS_Comprehensive_Exercise_Guide.pdf](EIMS_Comprehensive_Exercise_Guide.pdf) | 生成先の `output/pdf/`、受講者配布資料内の共通機能PDF |
| 追加仕様 | [EIMS_追加機能設計書.pdf](EIMS_追加機能設計書.pdf) | 生成スクリプトのoutput、受講者配布資料内の追加機能PDF |

`追加機能の設計書/EIMS_追加機能設計書_260722.pdf` は旧版として保管し、今回の配布ZIPには含めません。日付付きの旧版を現行版と取り違えないでください。
共通仕様の横長の大きな図は、文字を読みやすくするためA3横のページで収録しています。本文はA4です。

### 作業記録・生成物・参考資料

| ファイル・フォルダー | 内容 |
| --- | --- |
| `output/pdf/` | 共通ガイドのPDF生成先と、過去の差分整合表 |
| `outputs/` | 過去に作成したテスト結果・テストケースExcel。セッションIDや作成時の名称で分かれている |
| `brain/.../scratch/` | 資料作成時の作業ファイル |
| `.tmp-additional-design-doc/`、`tmp/`、各 `tmp/` | PDF生成・確認用の一時HTML、画像、ブラウザプロファイル等 |
| `追加機能の設計書/review-evidence/` | レビュー確認用の画面画像 |
| `extract_pdf.py`、`pdf_*.txt` | 参考PDFからの文字抽出処理とその中間テキスト。正式な仕様本文ではない |
| `take_screenshots.py` | 共通画面の画像作成用。旧Cドライブの絶対パスを含む |
| `take_additional_screenshots.mjs` | 追加画面の画像作成用。撮影準備でDB更新も行うため、単なる閲覧用ではない |
| `参考ファイル/` | Spring Boot教材、個人演習資料、参考解答等。EIMSの現行仕様とは分けて参照する |
| `pdf変換及び装飾関連/総合演習ガイド.md`、同 `build-guide-pdf.js` | **別教材のSPA入門・書籍管理システム**。EIMS共通ガイドの原稿・生成処理ではない |
| `pdf変換及び装飾関連/build-alignment-report-pdf.py`、同 `alignment-report.css` | EIMSの差分整合表用。同じフォルダーに別教材の生成処理が混在している |
| `.vscode/` | エディター設定 |

## 次に配置を整理する場合

承認に基づき、同じ場所の別名コピー（共通PDF2件・追加解答ZIP1件）を削除しました。原稿・ソースなどの原本と、PDF・ZIPの配布用コピーは別ファイルで残しています。追加承認の対象であるルートDBSETとスターター内DBSETの `old` 配下は削除し、任意課題用の `addEmployeesForPaging.sql` は残しています。それ以外の内容の異なる旧版の削除や、大幅な配置変更は行っていません。
次の順番で整理すると、必要な原本や配布物を誤って失うのを防げます。

1. このREADMEの現行原稿・配布PDFを基準として、旧版の保管先を決める。
2. 配布ZIPの再生成・照合手順を維持する。
3. 仕様原稿・画像、受講者配布物、講師解答、作業記録、別教材の単位で移動先を決める。
4. Markdownの画像参照、PDF生成スクリプト、テスト実行バッチのパスを合わせて更新する。
5. 中間生成物のGit除外と、重複ファイルの保管・削除方針を決める。

無関係な既存ファイルの削除は今回の変更に含めません。配布元として使用する解答ソース・テスト・画像の既存差分は、今回の新規修正と区別して検証報告に記録しています。

## 配布物の再生成

Node.js、Python 3、Pandoc、Google Chromeが必要です。PandocはPATHに登録してください。リポジトリ直下で実行します。

```powershell
npm ci --prefix tools
node tools/build-common-pdf.mjs
node "追加機能の設計書/build-additional-pdf.js"
python tools/package-distribution.py
```

PDFを修正した場合は、配布前に画像へ変換して図表・改ページを確認してください。最後のコマンドで、ルートDBSETからスターターへの同種SQL・バッチの同期、PDFのコピー、7種類のZIP生成、ZIP内全ファイルと配布元の照合を行います。`old` は配布ZIPから除外し、スターター固有の `addEmployeesForPaging.sql` は残します。
結果のSHA-256とファイル件数は [配布物照合結果](docs/配布物照合結果.json) に出力します。

解答ZIPは `src/`、Maven Wrapper、`pom.xml`、案内を収録し、`target/`、IDE設定、バックアップテスト、ブラウザテストの作業環境を除外します。
受講者用ZIPには講師用解答を含めません。
