-- ===== DB作成 =====
CREATE DATABASE IF NOT EXISTS eimsdb_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE eimsdb_test;

-- ===== 部署テーブル =====
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
  deptno   INTEGER,
  deptname VARCHAR(10) NOT NULL,
  PRIMARY KEY (deptno)
) ENGINE=InnoDB;

-- ===== 社員テーブル（role / delete_flg 追加版）=====
CREATE TABLE employee (
  empno      INTEGER      NOT NULL AUTO_INCREMENT,
  lname      VARCHAR(20)  NOT NULL,
  fname      VARCHAR(20)  NOT NULL,
  lkana      VARCHAR(50)  NOT NULL,
  fkana      VARCHAR(50)  NOT NULL,
  password   VARCHAR(30)  NOT NULL,
  gender     INTEGER      NOT NULL,
  deptno     INTEGER      NOT NULL,
  role       INTEGER      DEFAULT 0,         -- 0:一般 / 1:管理者
  delete_flg INTEGER      DEFAULT 0,         -- 0:在籍 / 1:退職
  PRIMARY KEY (empno),
  CONSTRAINT fk_employee_department
    FOREIGN KEY (deptno) REFERENCES department(deptno)
) ENGINE=InnoDB;

-- ===== 初期データ投入 =====
-- 部署
INSERT INTO department (deptno, deptname) VALUES
(100, '人事部'),
(200, '経理部'),
(300, '営業部'),
(400, '企画部'),
(500, '開発部'),
(600, '総務部');

-- 社員（列名を明示。role / delete_flg は既定値0が入る）
INSERT INTO employee (empno, lname, fname, lkana, fkana, password, gender, deptno) VALUES
(10001, '長嶋', '陽翔', 'ナガシマ', 'ヒナタ', 'password', 1, 100),
(10002, '中田', '結衣', 'ナカタ', 'ユイ', 'password', 2, 400),
(10003, '松井', '大翔', 'マツイ', 'ヒロト', 'password', 1, 100),
(10004, '丸山', '美咲', 'マルヤマ', 'ミサキ', 'password', 2, 100),
(10005, '募部', '莉子', 'ボブ', 'リコ', 'password', 2, 300),
(10006, '中村', '一郎', 'ナカムラ', 'イチロウ', 'password', 1, 100),
(10007, '大河', '芽依', 'タイガ', 'メイ', 'password', 2, 500),
(10008, '武', '悠真', 'タケ', 'ユウマ', 'password', 1, 100),
(10009, '木村', '陽菜', 'キムラ', 'ヒナ', 'password', 2, 400),
(10010, '中居', '海斗', 'ナカイ', 'カイト', 'password', 1, 300),
(10011, '北野', '愛莉', 'キタノ', 'アイリ', 'password', 2, 300),
(10012, '松本', '紬', 'マツモト', 'ツムギ', 'password', 2, 500),
(10013, '中村', '樹', 'ナカムラ', 'イツキ', 'password', 1, 300),
(10014, '明石屋', '結菜', 'アカシヤ', 'ユイナ', 'password', 2, 100),
(10015, '織田', '美月', 'オダ', 'ミヅキ', 'password', 2, 500),
(10016, '田村', '蒼空', 'タムラ', 'ソラ', 'password', 1, 300),
(10017, '田中', '桜', 'タナカ', 'サクラ', 'password', 2, 500),
(10018, '刈呂巣', '舞', 'カルロス', 'マイ', 'password', 2, 500),
(10019, '西田', '葵', 'ニシダ', 'アオイ', 'password', 2, 400),
(10020, '大竹', '悠真', 'オオタケ', 'ユウマ', 'password', 1, 100),
(10021, '松井', '美羽', 'マツイ', 'ミウ', 'password', 2, 600),
(10022, '明', '陽葵', 'アケ', 'ヒマリ', 'password', 2, 600),
(10023, '小田切', '梨奈', 'オダギリ', 'リナ', 'password', 2, 100),
(10024, '胴元', '彩乃', 'ドウモト', 'アヤノ', 'password', 2, 200),
(10025, '胴元', '美紀', 'ドウモト', 'ミキ', 'password', 2, 300),
(10026, '絵口', '翔太', 'エグチ', 'ショウタ', 'password', 1, 400),
(10027, '江森', '春菜', 'エモリ', 'ハルナ', 'password', 2, 500),
(10028, '梅実屋', '愛美', 'ウメミヤ', 'マナミ', 'password', 2, 600),
(10029, '泉矢', '咲良', 'イズミヤ', 'サクラ', 'password', 2, 100),
(10030, '意思塚', '舞', 'イシヅカ', 'マイ', 'password', 2, 200),
(10031, '高橋', '結衣', 'タカハシ', 'ユイ', 'password', 2, 400),
(10032, '中田', '大翔', 'ナカタ', 'ヒロト', 'password', 1, 400),
(10033, '田村', '莉子', 'タムラ', 'リコ', 'password', 2, 200),
(10034, '丸山', '悠真', 'マルヤマ', 'ユウマ', 'password', 1, 100),
(10035, '浜崎', '陽菜', 'ハマザキ', 'ヒナ', 'password', 2, 400),
(10036, '中村', '翼', 'ナカムラ', 'ツバサ', 'password', 1, 100),
(10037, '黒木', '紬', 'クロキ', 'ツムギ', 'password', 2, 200),
(10038, '武', '大輝', 'タケ', 'ダイキ', 'password', 1, 100),
(10039, '森', '美月', 'モリ', 'ミヅキ', 'password', 2, 400),
(10040, '木村', '颯太', 'キムラ', 'ソウタ', 'password', 1, 400),
(10041, '松嶋', '舞', 'マツシマ', 'マイ', 'password', 2, 500),
(10042, '松本', '陽太', 'マツモト', 'ヨウタ', 'password', 1, 500),
(10043, '後藤', '愛莉', 'ゴトウ', 'アイリ', 'password', 2, 600),
(10044, '明石屋', '悠斗', 'アカシヤ', 'ユウト', 'password', 1, 100),
(10045, '柴崎', '結菜', 'シバサキ', 'ユイナ', 'password', 2, 600),
(10046, '田村', '駿', 'タムラ', 'シュン', 'password', 1, 300),
(10047, '竹内', '美羽', 'タケウチ', 'ミウ', 'password', 2, 400),
(10048, '田中', '颯', 'タナカ', 'ハヤテ', 'password', 1, 500),
(10049, '中島', '陽葵', 'ナカジマ', 'ヒマリ', 'password', 2, 400),
(10050, '大竹', '樹', 'オオタケ', 'イツキ', 'password', 1, 100),
(10051, '宇多田', '梨奈', 'ウタダ', 'リナ', 'password', 2, 400),
(10052, '明', '彩乃', 'アケ', 'アヤノ', 'password', 2, 600),
(10053, '小田切', '隼人', 'オダギリ', 'ハヤト', 'password', 1, 100),
(10054, '胴元', '美緒', 'ドウモト', 'ミオ', 'password', 2, 200),
(10055, '胴元', '晴翔', 'ドウモト', 'ハルト', 'password', 1, 300),
(10056, '絵口', '春菜', 'エグチ', 'ハルナ', 'password', 2, 400),
(10057, '江森', '愛美', 'エモリ', 'マナミ', 'password', 2, 500),
(10058, '梅実屋', '咲良', 'ウメミヤ', 'サクラ', 'password', 2, 600),
(10059, '泉矢', '舞', 'イズミヤ', 'マイ', 'password', 2, 100),
(10060, '意思塚', '翔太', 'イシヅカ', 'ショウタ', 'password', 1, 200);

-- ===== 追加要件の反映 =====
-- 管理者（10001）は role=1
UPDATE employee
  SET role = 1
WHERE empno = 10001;

-- 退職者（10043, 10045）は delete_flg=1
UPDATE employee
  SET delete_flg = 1
WHERE empno IN (10043, 10045);

quit
