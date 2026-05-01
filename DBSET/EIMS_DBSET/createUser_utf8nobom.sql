-- ユーザの作成

-- ユーザ名：eimsuser
-- パスワード：eimspass

-- GRANT ALL PRIVILEGES ON eimsdb.* TO eimsuser IDENTIFIED BY 'eimspass';
-- GRANT ALL PRIVILEGES ON eimsdb.* TO 'eimsuser'@'localhost' IDENTIFIED BY 'eimspass';

CREATE USER 'eimsuser'@'localhost' IDENTIFIED BY 'eimspass';
GRANT all ON eimsdb.* TO 'eimsuser'@'localhost';
FLUSH PRIVILEGES;



quit
