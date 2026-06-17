@echo off
rem ===dbset.bat===
rem ■使用時の前提条件
rem 　１）データベースを作成する先はローカルコンピュータである
rem 　２）rootユーザのパスワードはPa$$w0rdである
rem 

mysql --default-character-set=utf8mb4 -uroot -pPa$$w0rd < dropUser.sql
mysql --default-character-set=utf8mb4 -uroot -pPa$$w0rd < dropDB.sql
mysql --default-character-set=utf8mb4 -uroot -pPa$$w0rd < createUser.sql
mysql --default-character-set=utf8mb4 -uroot -pPa$$w0rd < createDB.sql
echo =========================================================
echo データベースの作成が終了しました。
echo もし、エラーが発生している場合は再度、やり直してください。
echo =========================================================
pause