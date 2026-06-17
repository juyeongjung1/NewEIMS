@echo off

mysql --default-character-set=utf8mb4 -uroot -pPa$$w0rd < dropUser.sql
mysql --default-character-set=utf8mb4 -uroot -pPa$$w0rd < dropDB.sql
echo =========================================================
echo データベースの削除が終了しました。
echo もし、エラーが発生している場合は再度、やり直してください。
echo =========================================================
pause
