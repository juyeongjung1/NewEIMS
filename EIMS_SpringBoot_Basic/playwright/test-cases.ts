export type AssessmentLevel = 'common' | 'standard' | 'reference';
export type FeatureId = 'search' | 'registration' | 'update' | 'delete';

export type EimsTestCase = {
  id: string;
  feature: FeatureId;
  title: string;
  description: string;
  input: string;
  expected: string;
  note: string;
  level: AssessmentLevel;
  hints: string[];
};

export type SearchTestCase = EimsTestCase;

const searchTestCasesWithoutFeature: Omit<EimsTestCase, 'feature'>[] = [
  {
    id: 'TC001', title: '検索画面表示', level: 'common',
    description: 'トップページから検索画面へ遷移できることを確認する。',
    input: 'トップページで「検索」をクリックする。',
    expected: '検索画面 search が表示される。',
    note: 'UC002 基本フロー、GET /search',
    hints: ['index.htmlに検索画面へのリンクがあるか確認してください。', 'GET /searchのControllerメソッドを確認してください。'],
  },
  {
    id: 'TC002', title: '部署プルダウン表示', level: 'standard',
    description: '検索画面で部署一覧がDBから取得されることを確認する。',
    input: '検索画面の部署プルダウンを開く。',
    expected: '人事部(100)、経理部(200)、営業部(300)、総務部(400)、開発部(500)、企画部(600)が選択肢として表示される。',
    note: '6.4 初期データ仕様',
    hints: ['一般仕様では部署をselect要素で表示します。', '検索画面表示時に部署一覧をModelへ設定しているか確認してください。'],
  },
  {
    id: 'TC003', title: '社員番号検索（正常）', level: 'common',
    description: '存在する社員番号で検索すると詳細画面へ直接遷移することを確認する。',
    input: '社員番号: 10001 を入力し検索する。',
    expected: '検索結果一覧を経由せず、社員番号10001の社員詳細画面が表示される。',
    note: 'UC002 alt4-2、GET /selectByEmpNo',
    hints: ['社員番号検索のフォーム送信先を確認してください。', '社員が見つかった場合にemployee_detailを返しているか確認してください。'],
  },
  {
    id: 'TC004', title: '社員番号検索（該当なし）', level: 'standard',
    description: '存在しない社員番号で検索した場合の表示を確認する。',
    input: '社員番号: 99999 を入力し検索する。',
    expected: '「検索条件に一致する社員は見つかりませんでした。」が表示される。',
    note: 'UC002 alt4-1',
    hints: ['検索結果が0件の場合の分岐を確認してください。', 'search_result.htmlの0件メッセージを確認してください。'],
  },
  {
    id: 'TC005', title: '社員番号検索（未入力）', level: 'standard',
    description: '社員番号が未入力の場合に検索画面へ留まることを確認する。',
    input: '社員番号を空欄のまま検索する。',
    expected: '検索画面に留まり、必要に応じて入力を促すエラー表示が行われる。',
    note: '1.1.2 共通の挙動ルール',
    hints: ['empNoがnullの場合の処理を確認してください。', '検索画面を再表示する際に部署一覧も設定してください。'],
  },
  {
    id: 'TC006', title: '社員番号検索（数値以外）', level: 'reference',
    description: '社員番号に数値以外が入力できない、または不正として扱われることを確認する。',
    input: '社員番号: ABC を入力して検索する。',
    expected: 'ブラウザーまたはサーバー側で不正入力として扱われ、検索処理が実行されない。',
    note: '社員番号は主キーの数値項目。実装方式に応じて判定する。',
    hints: ['社員番号入力欄をtype="number"にするとブラウザー側でも制御できます。'],
  },
  {
    id: 'TC007', title: '氏名検索（氏の部分一致）', level: 'common',
    description: '氏にキーワードを含む社員が検索されることを確認する。',
    input: '社員名: 田 を入力し検索する。',
    expected: '氏または名に「田」を含む社員が検索結果一覧に表示される。',
    note: '1.1.2 社員名検索。初期データに合わせて件数を確認する。',
    hints: ['部分一致検索のRepositoryメソッドを確認してください。', '検索結果をemployeesとしてModelへ設定しているか確認してください。'],
  },
  {
    id: 'TC008', title: '氏名検索（名の部分一致）', level: 'standard',
    description: '名にキーワードを含む社員が検索されることを確認する。',
    input: '社員名に初期データの名の一部「陽」を入力し検索する。',
    expected: '名に「陽」を含む社員が検索結果一覧に表示される。',
    note: '氏または名のOR検索であること。',
    hints: ['一般仕様ではlastNameとfirstNameの両方をOR検索します。', '簡易実装の氏だけの検索では、この項目は未達になります。'],
  },
  {
    id: 'TC009', title: '氏名検索（複数件）', level: 'common',
    description: '氏名検索で複数件が一覧表示されることを確認する。',
    input: '複数社員に一致するキーワード「中」を入力し検索する。',
    expected: '該当する複数社員が検索結果一覧に表示される。',
    note: '検索結果表の列構成も同時に確認する。',
    hints: ['検索結果を1件だけ取得していないか確認してください。', 'Thymeleafのth:eachを確認してください。'],
  },
  {
    id: 'TC010', title: '氏名検索（該当なし）', level: 'standard',
    description: '氏名検索で該当0件の場合の表示を確認する。',
    input: '社員名: 存在しない文字列 を入力し検索する。',
    expected: 'テーブルを表示せず「検索条件に一致する社員は見つかりませんでした。」が表示される。',
    note: 'UC002 alt4-1、1.1.3',
    hints: ['一般仕様では0件時に表を隠してメッセージを表示します。', '簡易実装では空の表でも許容されます。'],
  },
  {
    id: 'TC011', title: '氏名検索（未入力）', level: 'standard',
    description: '検索キーワードが空の場合に検索画面へ留まることを確認する。',
    input: '社員名を空欄のまま検索する。',
    expected: '検索画面に留まり、検索結果画面へ遷移しない。',
    note: '1.1.2 共通の挙動ルール',
    hints: ['keywordがnullまたは空文字の場合の分岐を確認してください。'],
  },
  {
    id: 'TC012', title: '氏名検索（空白のみは未入力）', level: 'standard',
    description: '空白のみのキーワードが未入力として扱われることを確認する。',
    input: '社員名キーワードに半角スペースのみを入力して検索する。',
    expected: '未入力として扱われ、検索結果へ遷移せず検索画面が再表示される。',
    note: '前後空白を含むキーワードのトリム検索は対象外。空白のみはisBlank判定で未入力扱い。',
    hints: ['String.isBlank()などで空白だけの文字列を判定できます。'],
  },
  {
    id: 'TC013', title: '部署検索（正常）', level: 'common',
    description: '選択または入力した部署の社員が表示されることを確認する。',
    input: '部署: 人事部（部署番号100）を指定し検索する。',
    expected: '人事部に所属する社員が検索結果一覧に表示される。',
    note: 'GET /selectByDeptNo。簡易実装の部署番号テキスト入力も許容する。',
    hints: ['deptNo=100で検索できるか確認してください。', '部署の指定方法はプルダウンとテキスト入力の両方を判定できます。'],
  },
  {
    id: 'TC014', title: '部署検索（他部署）', level: 'common',
    description: '別部署を選択した場合に該当部署の社員のみ表示されることを確認する。',
    input: '部署: 営業部（部署番号300）を指定し検索する。',
    expected: '営業部に所属する社員のみ検索結果一覧に表示される。',
    note: '部署名とdept_noの対応を確認する。',
    hints: ['検索条件のdeptNoがRepositoryまで渡っているか確認してください。'],
  },
  {
    id: 'TC015', title: '部署検索（未選択）', level: 'standard',
    description: '部署が未選択の場合の扱いを確認する。',
    input: '部署を選択せずに検索する。',
    expected: '検索画面に留まる、または入力を促すエラーが表示される。',
    note: '標準実装の部署プルダウン前提。',
    hints: ['deptNoがnullの場合に検索画面を再表示してください。'],
  },
  {
    id: 'TC016', title: '検索結果一覧の列構成', level: 'standard',
    description: '検索結果一覧の表示項目が仕様通りであることを確認する。',
    input: '氏名検索または部署検索で検索結果一覧を表示する。',
    expected: '表は「社員番号」「氏名+カナ」「性別」「部署名」の4列で構成される。',
    note: '1.1.3 一覧画面（共通ルール）',
    hints: ['search_result.htmlのtheadを確認してください。', '一般仕様では部署コードではなく部署名を表示します。'],
  },
  {
    id: 'TC017', title: '検索結果一覧の氏名リンク', level: 'common',
    description: '検索結果一覧から社員詳細へ遷移できることを確認する。',
    input: '検索結果一覧で任意社員の氏名+カナをクリックする。',
    expected: '対象社員の社員詳細画面が表示される。',
    note: 'UC002 alt5-3',
    hints: ['氏名表示を/detail/{empNo}へのリンクにしてください。'],
  },
  {
    id: 'TC018', title: '検索結果から再検索', level: 'common',
    description: '検索結果画面から検索画面へ戻れることを確認する。',
    input: '検索結果画面で「検索画面に戻る」をクリックする。',
    expected: '部署一覧を再取得した検索画面が表示される。',
    note: 'UC002 alt5-1',
    hints: ['search_result.htmlに/searchへの導線を配置してください。'],
  },
  {
    id: 'TC019', title: '検索結果からトップページへ戻る', level: 'common',
    description: '検索結果画面からトップページへ戻れることを確認する。',
    input: '検索結果画面で「メニューに戻る」をクリックする。',
    expected: 'トップページが正常に表示される。',
    note: 'UC002 alt5-2',
    hints: ['search_result.htmlにトップページへの導線を配置してください。'],
  },
  {
    id: 'TC020', title: '0件時の表非表示', level: 'standard',
    description: '0件時に空の表ではなくメッセージが表示されることを確認する。',
    input: '該当なしとなる条件で検索する。',
    expected: '検索結果テーブルは表示されず、0件メッセージのみ表示される。',
    note: '標準実装。簡略化実装の場合は空表表示も許容する。',
    hints: ['一般仕様ではemployees.emptyのときにtableを非表示にします。'],
  },
  {
    id: 'TC021', title: '検索画面の入力欄独立性', level: 'common',
    description: '社員番号・社員名・部署の検索条件が意図通り扱われることを確認する。',
    input: '社員番号と社員名を同時に入力し、社員番号側の検索ボタンを押す。',
    expected: '押下した検索ボタンに対応する条件で検索され、意図しない複合検索にならない。',
    note: '各フォーム/ボタンの実装仕様に合わせて確認する。',
    hints: ['検索方式ごとにformと送信先が分かれているか確認してください。'],
  },
  {
    id: 'TC022', title: 'DBアクセス異常時', level: 'reference',
    description: '検索時にDBへ接続できない場合の異常系表示を確認する。',
    input: 'DBを停止した状態で検索画面表示または検索を実行する。',
    expected: 'システムエラー画面またはDBアクセス不可を示すエラーが表示され、アプリが停止しない。',
    note: '実施可能な環境でのみ確認。今回の自動テストではDBを停止しないため実施対象外。',
    hints: ['この項目は環境操作が必要なため自動実行しません。'],
  },
  {
    id: 'TC023', title: '社員番号検索（負数）', level: 'reference',
    description: '負の社員番号で検索した場合の表示を確認する。',
    input: '社員番号: -1 を入力し検索する。',
    expected: '0件メッセージが表示される、または不正入力として検索画面に留まる。',
    note: '社員番号は正の採番値であるため異常系として確認。',
    hints: ['負数を検索してもサーバーエラーにならないことを確認してください。'],
  },
  {
    id: 'TC024', title: '社員番号検索（小数）', level: 'reference',
    description: '小数の社員番号で検索した場合の入力制御を確認する。',
    input: '社員番号: 10001.5 を入力し検索する。',
    expected: 'ブラウザーまたはサーバー側で不正入力として扱われ、想定外の社員詳細へ遷移しない。',
    note: '数値入力欄/型変換の確認。',
    hints: ['社員番号入力欄のtypeとサーバー側の型変換を確認してください。'],
  },
  {
    id: 'TC025', title: '部署検索（存在しない部署番号の直接指定）', level: 'reference',
    description: '存在しない部署番号をURLパラメーターで直接指定した場合の制御を確認する。',
    input: 'URL「/selectByDeptNo?deptNo=999」を直接実行する。',
    expected: '検索結果0件のメッセージが表示される、空の表が表示される、または不正入力として扱われる。',
    note: 'プルダウン外の値を直接送信する異常系。',
    hints: ['存在しない部署番号でも500エラーにならないことを確認してください。'],
  },
  {
    id: 'TC026', title: '部署検索（不正形式の部署番号）', level: 'reference',
    description: '数値以外の部署番号をURLパラメーターで直接指定した場合の制御を確認する。',
    input: 'URL「/selectByDeptNo?deptNo=abc」を直接実行する。',
    expected: '400エラー、エラー画面、または不正リクエストとして扱われ、アプリが停止しない。',
    note: '型不一致の異常系。',
    hints: ['不正リクエスト後もトップページへアクセスできることを確認してください。'],
  },
  {
    id: 'TC027', title: '氏名検索（極端に長い文字列）', level: 'reference',
    description: '極端に長い検索キーワードで検索した場合の制御を確認する。',
    input: '社員名に256文字程度の文字列を入力して検索する。',
    expected: '検索結果0件または入力エラーとして扱われ、画面崩れやサーバーエラーが発生しない。',
    note: '仕様上文字数制限は明記なし。堅牢性確認として実施。',
    hints: ['長い検索文字列でも500エラーを発生させないようにしてください。'],
  },
  {
    id: 'TC028', title: '検索結果の氏名+カナ連結形式', level: 'common',
    description: '検索結果一覧の氏名+カナが連結フォーマットで表示されることを確認する。',
    input: '氏名検索で複数件ヒットさせる。',
    expected: '各行の氏名+カナが「氏 名 (氏カナ 名カナ)」の形式（半角スペース連結）で表示される。',
    note: '1.1.3 連結ルール。列構成（TC016）とは別観点。',
    hints: ['氏と名、氏カナと名カナの間に半角スペースを入れてください。'],
  },
  {
    id: 'TC029', title: '検索画面からメニューに戻る', level: 'common',
    description: '検索条件入力画面の「メニューに戻る」導線を確認する。',
    input: '検索画面で「メニューに戻る」をクリックする。',
    expected: 'トップページ（index）が表示される。',
    note: '1.1.2／UC002。検索結果からの戻り（TC019）とは別画面。',
    hints: ['search.htmlにトップページへの導線を配置してください。'],
  },
];

export const searchTestCases: SearchTestCase[] = searchTestCasesWithoutFeature.map((testCase) => ({
  ...testCase,
  feature: 'search',
}));

export const searchTestCaseMap = new Map(searchTestCases.map((testCase) => [testCase.id, testCase]));
