-- This script creates a database and two user entries for that database.
-- Replace CAPITALIZED TOKENS for other databases, users, passwords, etc.
drop database if exists CDS;
create database CDS;
create user 'CDS_USER'@'localhost' identified by 'CDS_PASS';
grant all on CDS.* to 'CDS_USER'@'localhost';
create user 'CCDS_USER'@'%' identified by 'CDS_PASS';
grant all on CDS.* to 'CDS_USER'@'%';
