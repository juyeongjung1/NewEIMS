-- ===== DB作成 =====
CREATE DATABASE IF NOT EXISTS eimsdb_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE eimsdb_test;

-- ===== 部署テーブル =====
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
  dept_no   INTEGER      NOT NULL,
  dept_name VARCHAR(10) NOT NULL,
  PRIMARY KEY (dept_no)
) ENGINE=InnoDB;

-- ===== 社員テーブル（role / delete_flg 追加版）=====
CREATE TABLE employee (
  emp_no     INTEGER      NOT NULL AUTO_INCREMENT,
  last_name     VARCHAR(10)  NOT NULL,
  first_name     VARCHAR(10)  NOT NULL,
  last_kana     VARCHAR(20)  NOT NULL,
  first_kana     VARCHAR(20)  NOT NULL,
  password   VARCHAR(20)  NOT NULL,
  gender     INTEGER      NOT NULL,
  dept_no    INTEGER      NOT NULL,
  role       INTEGER      NOT NULL DEFAULT 0, -- 0:一般 / 1:管理者
  delete_flg INTEGER      NOT NULL DEFAULT 0, -- 0:在籍 / 1:退職
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
(400, '総務部'),
(500, '開発部'),
(600, '企画部');

-- 社員（氏名を明示。role / delete_flg は既定値0が入る）
INSERT INTO employee (emp_no, last_name, first_name, last_kana, first_kana, password, gender, dept_no) VALUES
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
(10020, '大竹', '悠真', 'オオタケ', 'ユウマ', 'password', 1, 100);

-- ===== 追加プロパティの反映 =====
-- 管理者（10001）の role=1
UPDATE employee
  SET role = 1
WHERE emp_no = 10001;

quit
