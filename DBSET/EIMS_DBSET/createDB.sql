-- ===== DB作成 =====
CREATE DATABASE IF NOT EXISTS eimsdb CHARACTER SET utf8mb4;

USE eimsdb;

-- 部署テーブル
CREATE TABLE department (
  dept_no   INTEGER      NOT NULL,
  dept_name VARCHAR(20)  NOT NULL,
  PRIMARY KEY (dept_no)
) ENGINE=InnoDB;

-- 社員テーブル
CREATE TABLE employee (
  emp_no     INTEGER      NOT NULL AUTO_INCREMENT,
  last_name     VARCHAR(10)  NOT NULL,
  first_name    VARCHAR(10)  NOT NULL,
  last_kana     VARCHAR(20)  NOT NULL,
  first_kana    VARCHAR(20)  NOT NULL,
  password      VARCHAR(20)  NOT NULL,
  gender        INTEGER      NOT NULL,
  dept_no       INTEGER,
  role          INTEGER      NOT NULL DEFAULT 0,
  delete_flg    INTEGER      NOT NULL DEFAULT 0,
  PRIMARY KEY (emp_no),
  CONSTRAINT fk_employee_department
    FOREIGN KEY (dept_no) REFERENCES department(dept_no)
) ENGINE=InnoDB;

-- ===== 初期データ投入 =====
-- 部署
INSERT INTO department (dept_no, dept_name) VALUES
(100, '人事部'),
(200, '経理部'),
(300, '営業部'),
(400, '企画部'),
(500, '開発部'),
(600, '総務部');

-- 社員（氏名を例示。role / delete_flg は既定値0が入る）
INSERT INTO employee (emp_no, last_name, first_name, last_kana, first_kana, password, gender, dept_no) VALUES
(10001, '長嶋', '陽翔', 'ナガシマ', 'ヒナタ', 'password', 1, 100),
(10002, '中田', '結衣', 'ナカタ', 'ユイ', 'password', 2, 400),
(10003, '松井', '大翔', 'マツイ', 'ヒロト', 'password', 1, 100),
(10004, '丸山', '美咲', 'マルヤマ', 'ミサキ', 'password', 2, 100),
(10005, 'ボブ', '莉子', 'ボブ', 'リコ', 'password', 2, 300),
(10006, '中村', '一郎', 'ナカムラ', 'イチロウ', 'password', 1, 100),
(10007, '佐藤', '花子', 'サトウ', 'ハナコ', 'password', 2, 200),
(10008, '鈴木', '次郎', 'スズキ', 'ジロウ', 'password', 1, 500),
(10009, '高橋', '三郎', 'タカハシ', 'サブロウ', 'password', 1, 300),
(10010, '田中', '四郎', 'タナカ', 'シロウ', 'password', 1, 400),
(10011, '伊藤', '五郎', 'イトウ', 'ゴロウ', 'password', 1, 500),
(10012, '渡辺', '六郎', 'ワタナベ', 'ロクロウ', 'password', 1, 600),
(10013, '山本', '七郎', 'ヤマモト', 'シチロウ', 'password', 1, 100),
(10014, '中村', '八郎', 'ナカムラ', 'ハチロウ', 'password', 1, 200),
(10015, '小林', '九郎', 'コバヤシ', 'キュウロウ', 'password', 1, 300),
(10016, '加藤', '十郎', 'カトウ', 'ジュウロウ', 'password', 1, 400),
(10017, '吉田', '十一郎', 'ヨシダ', 'ジュウイチロウ', 'password', 1, 500),
(10018, '山田', '十二郎', 'ヤマダ', 'ジュウニロウ', 'password', 1, 600),
(10019, '佐々木', '十三郎', 'ササキ', 'ジュウサンロウ', 'password', 1, 100),
(10020, '山口', '十四郎', 'ヤマグチ', 'ジュウシロウ', 'password', 1, 200);

-- 管理者（10001）の role=1
UPDATE employee
  SET role = 1
WHERE emp_no = 10001;
