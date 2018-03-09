-- Script to downgrade database used by the Common Data Service
-- FROM version 1.9.x TO version 1.8.x.
-- No database is created or specified to allow flexible deployment!

-- Undo: 9 Change type
ALTER TABLE C_SOLUTION MODIFY PROVIDER CHAR(64) NULL;
-- Undo: 8 Add a column
ALTER TABLE C_PEER DROP COLUMN TRUST_LEVEL;
-- Undo: 7 Add a row
DELETE FROM C_SITE_CONFIG WHERE CONFIG_KEY = 'site_config';
-- Undo: 6 Drop the column
ALTER TABLE C_COMMENT ADD COLUMN URL VARCHAR(512) NOT NULL;
-- Undo: 5 Add the column
ALTER TABLE C_THREAD DROP COLUMN REVISION_ID;
-- Undo: 4 Add the column
ALTER TABLE C_THREAD DROP COLUMN SOLUTION_ID;
-- Undo: 3 Drop the column
ALTER TABLE C_THREAD ADD COLUMN URL VARCHAR(512) NOT NULL;
-- Undo:2  Make the column nullable
ALTER TABLE C_SITE_CONFIG MODIFY USER_ID CHAR(36) NOT NULL;
-- Undo: 1 Make the column wider
ALTER TABLE C_SITE_CONFIG MODIFY CONFIG_VAL VARCHAR(1024) NOT NULL;
