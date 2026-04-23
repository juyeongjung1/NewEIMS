-- ユーザの作成

-- ユーザ名：eimsuser_test
-- パスワード：eimspass

-- GRANT ALL PRIVILEGES ON eimsdb_test.* TO eimsuser_test IDENTIFIED BY 'eimspass';
-- GRANT ALL PRIVILEGES ON eimsdb_test.* TO 'eimsuser_test'@'localhost' IDENTIFIED BY 'eimspass';

CREATE USER 'eimsuser_test'@'localhost' IDENTIFIED BY 'eimspass';
GRANT all ON eimsdb_test.* TO 'eimsuser_test'@'localhost';
FLUSH PRIVILEGES;



quit
