@echo off
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < dropUser.sql
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < dropDB.sql
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < createUser.sql
mysql -uroot -pPa$$w0rd --default-character-set=utf8mb4 < createDB.sql
