-- ユーザの作成

-- ユーザ名：eimsuser
-- パスワード：Pa$$w0rd

CREATE USER IF NOT EXISTS 'eimsuser'@'localhost' IDENTIFIED BY 'Pa$$w0rd';
GRANT ALL PRIVILEGES ON eimsdb.* TO 'eimsuser'@'localhost';
FLUSH PRIVILEGES;

quit
