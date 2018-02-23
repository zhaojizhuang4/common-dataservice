-- ===============LICENSE_START=======================================================
-- Acumos Apache-2.0
-- ===================================================================================
-- Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
-- ===================================================================================
-- This Acumos software file is distributed by AT&T and Tech Mahindra
-- under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--  
--      http://www.apache.org/licenses/LICENSE-2.0
--  
-- This file is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- ===============LICENSE_END=========================================================

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
