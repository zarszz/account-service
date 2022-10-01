psql postgresql://postgres:password@localhost:5432/jetbrains_academy -c "CREATE SCHEMA IF NOT EXISTS auth;" &&

psql postgresql://postgres:password@localhost:5432/jetbrains_academy -c "CREATE TABLE IF NOT EXISTS auth."role" (
	id int8 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	code varchar(255) NOT NULL,
	name varchar(255) NULL,
	CONSTRAINT role_pkey PRIMARY KEY (id),
	CONSTRAINT uk_c36say97xydpmgigg38qv5l2p UNIQUE (code)
);" &&
psql postgresql://postgres:password@localhost:5432/jetbrains_academy -c "INSERT INTO auth."role" (code,name) VALUES ('USER','USER');" &&
psql postgresql://postgres:password@localhost:5432/jetbrains_academy -c "INSERT INTO auth."role" (code,name) VALUES ('ACCOUNTANT','ACCOUNTANT');" &&
psql postgresql://postgres:password@localhost:5432/jetbrains_academy -c "INSERT INTO auth."role" (code,name) VALUES ('ADMINISTRATOR','ADMINISTRATOR');" &&
psql postgresql://postgres:password@localhost:5432/jetbrains_academy -c "INSERT INTO auth."role" (code,name) VALUES ('AUDITOR','AUDITOR');"
