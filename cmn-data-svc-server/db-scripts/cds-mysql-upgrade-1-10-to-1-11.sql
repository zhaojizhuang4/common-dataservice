-- Script to upgrade database used by the Common Data Service
-- FROM version 1.10.x TO version 1.11.x.
-- No database is created or specified to allow flexible deployment!

-- 1 Make the column wider
ALTER TABLE C_SITE_CONFIG MODIFY CONFIG_VAL VARCHAR(8192) NOT NULL;
-- 2 Make the column nullable
ALTER TABLE C_SITE_CONFIG MODIFY USER_ID CHAR(36) NULL;
-- 3 Drop the column
ALTER TABLE C_THREAD DROP COLUMN URL;
-- 4 Add the column
ALTER TABLE C_THREAD ADD COLUMN SOLUTION_ID CHAR(36) NOT NULL;
-- 5 Add the column
ALTER TABLE C_THREAD ADD COLUMN REVISION_ID CHAR(36) NOT NULL;
-- 6 Drop the column
ALTER TABLE C_COMMENT DROP COLUMN URL;
-- 7 Add a row
INSERT INTO C_SITE_CONFIG (CONFIG_KEY, CONFIG_VAL) VALUES (
	'site_config', '{"fields":[{"type":"text","name":"siteInstanceName","label":"siteInstanceName","required":"true","data":"Acumos"},{"type":"file","name":"headerLogo","label":"Headerlogo","data":{"lastModified":1510831880727,"lastModifiedDate":"2017-11-16T11:31:20.727Z","name":"acumos_logo_white.png","size":3657,"type":"image/png"}},{"type":"file","name":"footerLogo","label":"Footerlogo","data":{"lastModified":1510831874776,"lastModifiedDate":"2017-11-16T11:31:14.776Z","name":"footer_logo.png","size":3127,"type":"image/png"}},{"type":"heading","name":"ConnectionConfig","label":"ConnectionConfig","required":"true","subFields":[{"type":"text","name":"socketTimeout","label":"socketTimeout","required":"true","data":"300"},{"type":"text","name":"connectionTimeout","label":"connectionTimeout","required":"true","data":"10"}]},{"type":"select","name":"enableOnBoarding","label":"EnableOnboarding","options":[{"name":"Enabled"},{"name":"Disabled"}],"required":true,"data":{"name":"Enabled"}}]}' );
-- 8 Add a column
ALTER TABLE C_PEER ADD COLUMN TRUST_LEVEL SMALLINT NOT NULL DEFAULT 0;
-- 9 Change type
ALTER TABLE C_SOLUTION MODIFY PROVIDER VARCHAR(64) NULL;