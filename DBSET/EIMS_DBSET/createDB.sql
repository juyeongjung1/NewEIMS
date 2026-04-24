-- ===== DB�쐬 =====
CREATE DATABASE IF NOT EXISTS eimsdb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE eimsdb;

-- ===== �����e�[�u�� =====
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
  deptno   INTEGER,
  deptname VARCHAR(10) NOT NULL,
  PRIMARY KEY (deptno)
) ENGINE=InnoDB;

-- ===== �Ј��e�[�u���irole / delete_flg �ǉ��Łj=====
CREATE TABLE employee (
  empno      INTEGER      NOT NULL AUTO_INCREMENT,
  lname      VARCHAR(10)  NOT NULL,
  fname      VARCHAR(10)  NOT NULL,
  lkana      VARCHAR(20)  NOT NULL,
  fkana      VARCHAR(20)  NOT NULL,
  password   VARCHAR(20)  NOT NULL,
  gender     INTEGER      NOT NULL,
  deptno     INTEGER      NOT NULL,
  role       INTEGER      DEFAULT 0,         -- 0:��� / 1:�Ǘ���
  delete_flg INTEGER      DEFAULT 0,         -- 0:�ݐ� / 1:�ސE
  PRIMARY KEY (empno),
  CONSTRAINT fk_employee_department
    FOREIGN KEY (deptno) REFERENCES department(deptno)
) ENGINE=InnoDB;

-- ===== �����f�[�^���� =====
-- ����
INSERT INTO department (deptno, deptname) VALUES
(100, '�l����'),
(200, '�o����'),
(300, '�c�ƕ�'),
(400, '��敔'),
(500, '�J����'),
(600, '������');

-- �Ј��i�񖼂𖾎��Brole / delete_flg �͊���l0������j
INSERT INTO employee (empno, lname, fname, lkana, fkana, password, gender, deptno) VALUES
(10001, '����', '�z��', '�i�K�V�}', '�q�i�^', 'password', 1, 100),
(10002, '���c', '����', '�i�J�^', '���C', 'password', 2, 400),
(10003, '����', '����', '�}�c�C', '�q���g', 'password', 1, 100),
(10004, '�ێR', '����', '�}�����}', '�~�T�L', 'password', 2, 100),
(10005, '�啔', '从q', '�{�u', '���R', 'password', 2, 300),
(10006, '����', '��Y', '�i�J����', '�C�`���E', 'password', 1, 100),
(10007, '���', '���', '�^�C�K', '���C', 'password', 2, 500),
(10008, '��', '�I�^', '�^�P', '���E�}', 'password', 1, 100),
(10009, '�ؑ�', '�z��', '�L����', '�q�i', 'password', 2, 400),
(10010, '����', '�C�l', '�i�J�C', '�J�C�g', 'password', 1, 300),
(10011, '�k��', '���', '�L�^�m', '�A�C��', 'password', 2, 300),
(10012, '���{', '��', '�}�c���g', '�c���M', 'password', 2, 500),
(10013, '����', '��', '�i�J����', '�C�c�L', 'password', 1, 300),
(10014, '���Ή�', '����', '�A�J�V��', '���C�i', 'password', 2, 100),
(10015, '�D�c', '����', '�I�_', '�~�d�L', 'password', 2, 500),
(10016, '�c��', '����', '�^����', '�\��', 'password', 1, 300),
(10017, '�c��', '��', '�^�i�J', '�T�N��', 'password', 2, 500),
(10018, '���C��', '��', '�J�����X', '�}�C', 'password', 2, 500),
(10019, '���c', '��', '�j�V�_', '�A�I�C', 'password', 2, 400),
(10020, '��|', '�I�^', '�I�I�^�P', '���E�}', 'password', 1, 100),
(10021, '����', '���H', '�}�c�C', '�~�E', 'password', 2, 600),
(10022, '��', '�z��', '�A�P', '�q�}��', 'password', 2, 600),
(10023, '���c��', '����', '�I�_�M��', '���i', 'password', 2, 100),
(10024, '����', '�ʔT', '�h�E���g', '�A���m', 'password', 2, 200),
(10025, '����', '���I', '�h�E���g', '�~�L', 'password', 2, 300),
(10026, '�G��', '�đ�', '�G�O�`', '�V���E�^', 'password', 1, 400),
(10027, '�]�X', '�t��', '�G����', '�n���i', 'password', 2, 500),
(10028, '�~����', '����', '�E���~��', '�}�i�~', 'password', 2, 600),
(10029, '���', '���', '�C�Y�~��', '�T�N��', 'password', 2, 100),
(10030, '�ӎv��', '��', '�C�V�d�J', '�}�C', 'password', 2, 200),
(10031, '����', '����', '�^�J�n�V', '���C', 'password', 2, 400),
(10032, '���c', '����', '�i�J�^', '�q���g', 'password', 1, 400),
(10033, '�c��', '从q', '�^����', '���R', 'password', 2, 200),
(10034, '�ێR', '�I�^', '�}�����}', '���E�}', 'password', 1, 100),
(10035, '�l��', '�z��', '�n�}�U�L', '�q�i', 'password', 2, 400),
(10036, '����', '��', '�i�J����', '�c�o�T', 'password', 1, 100),
(10037, '����', '��', '�N���L', '�c���M', 'password', 2, 200),
(10038, '��', '��P', '�^�P', '�_�C�L', 'password', 1, 100),
(10039, '�X', '����', '����', '�~�d�L', 'password', 2, 400),
(10040, '�ؑ�', '�D��', '�L����', '�\�E�^', 'password', 1, 400),
(10041, '����', '��', '�}�c�V�}', '�}�C', 'password', 2, 500),
(10042, '���{', '�z��', '�}�c���g', '���E�^', 'password', 1, 500),
(10043, '�㓡', '���', '�S�g�E', '�A�C��', 'password', 2, 600),
(10044, '���Ή�', '�I�l', '�A�J�V��', '���E�g', 'password', 1, 100),
(10045, '�č�', '����', '�V�o�T�L', '���C�i', 'password', 2, 600),
(10046, '�c��', '�x', '�^����', '�V����', 'password', 1, 300),
(10047, '�|��', '���H', '�^�P�E�`', '�~�E', 'password', 2, 400),
(10048, '�c��', '�D', '�^�i�J', '�n���e', 'password', 1, 500),
(10049, '����', '�z��', '�i�J�W�}', '�q�}��', 'password', 2, 400),
(10050, '��|', '��', '�I�I�^�P', '�C�c�L', 'password', 1, 100),
(10051, '�F���c', '����', '�E�^�_', '���i', 'password', 2, 400),
(10052, '��', '�ʔT', '�A�P', '�A���m', 'password', 2, 600),
(10053, '���c��', '���l', '�I�_�M��', '�n���g', 'password', 1, 100),
(10054, '����', '����', '�h�E���g', '�~�I', 'password', 2, 200),
(10055, '����', '����', '�h�E���g', '�n���g', 'password', 1, 300),
(10056, '�G��', '�t��', '�G�O�`', '�n���i', 'password', 2, 400),
(10057, '�]�X', '����', '�G����', '�}�i�~', 'password', 2, 500),
(10058, '�~����', '���', '�E���~��', '�T�N��', 'password', 2, 600),
(10059, '���', '��', '�C�Y�~��', '�}�C', 'password', 2, 100),
(10060, '�ӎv��', '�đ�', '�C�V�d�J', '�V���E�^', 'password', 1, 200);

-- ===== �ǉ��v���̔��f =====
-- �Ǘ��ҁi10001�j�� role=1
UPDATE employee
  SET role = 1
WHERE empno = 10001;

-- �ސE�ҁi10043, 10045�j�� delete_flg=1
UPDATE employee
  SET delete_flg = 1
WHERE empno IN (10043, 10045);


quit
