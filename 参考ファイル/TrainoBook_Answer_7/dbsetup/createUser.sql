-- 
-- ユーザ作成
--
CREATE USER IF NOT EXISTS 'book_user'@'localhost' IDENTIFIED BY 'book_pass';
GRANT ALL PRIVILEGES ON traino_book.* TO 'book_user'@'localhost';
FLUSH PRIVILEGES;

quit
