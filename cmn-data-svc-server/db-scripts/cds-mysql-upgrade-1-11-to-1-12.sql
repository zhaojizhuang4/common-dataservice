-- Script to upgrade database used by the Common Data Service
-- FROM version 1.11.x TO version 1.12.x.
-- No database is created or specified to allow flexible deployment!

-- WARNING!!  THIS ADDS A NON-NULL FOREIGN KEY COLUMN TO C_PEER_SUB!
-- WARNING!!  THIS ADDS A NON-NULL FOREIGN KEY COLUMN TO C_PEER_SUB!
-- Must set a default value based on the current state of the database
-- in step 1 below.

-- 1 add the column that will be a non-null foreign key
-- MUST CHANGE TO A GOOD DEFAULT VALUE
-- THE VALUE HERE IS ONLY AN EXAMPLE
ALTER TABLE C_PEER_SUB ADD COLUMN OWNER_ID CHAR(36) NOT NULL DEFAULT '1dc87f75-b07e-4b90-b706-afc1b5955d66';
-- 2 add the constraint
ALTER TABLE C_PEER_SUB ADD CONSTRAINT C_PEER_SUB_C_USER FOREIGN KEY (OWNER_ID) REFERENCES C_USER (USER_ID);
-- 3 add the column
ALTER TABLE C_SOLUTION ADD COLUMN SOURCE_ID CHAR(36) NULL;
-- 4 add the constraint
ALTER TABLE C_SOLUTION ADD CONSTRAINT C_SOLUTION_C_PEER FOREIGN KEY (SOURCE_ID) REFERENCES C_PEER (PEER_ID);
-- 5 add rows
INSERT INTO C_TOOLKIT_TYPE (TYPE_CD, TYPE_NAME) VALUES ('TC', 'Training Client');
INSERT INTO C_TOOLKIT_TYPE (TYPE_CD, TYPE_NAME) VALUES ('BR', 'Data Broker');
