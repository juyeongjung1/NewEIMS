@echo off
rem ===dbset.bat===
rem ■使用時の前提条件
rem 　１）データベースを作成する先はローカルコンピュータである
rem 　２）rootユーザのパスワードはPa$$w0rdである
rem 

mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < dropUser.sql
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < dropDB.sql
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < createUser.sql
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < createDB.sql
echo =========================================================
echo データベースの作成が終了しました。
echo もし、エラーが発生している場合は再度、やり直してください。
echo =========================================================
pause