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
-- FROM version 1.16.x TO version 1.15.x.
-- No database is specified to allow flexible deployment!

-- 14
DROP TABLE C_REVISION_DESC;
-- 13
ALTER TABLE C_USER DROP COLUMN VERIFY_EXPIRE_DATE;
-- 12
ALTER TABLE C_USER DROP COLUMN VERIFY_TOKEN_HASH;
-- 11
ALTER TABLE C_USER DROP COLUMN API_TOKEN;
-- 10
ALTER TABLE C_USER DROP COLUMN LOGIN_FAIL_DATE;
-- 9
ALTER TABLE C_USER DROP COLUMN LOGIN_FAIL_COUNT;
-- 8
ALTER TABLE C_SOLUTION_REV DROP COLUMN PUBLISHER;
-- 7
ALTER TABLE C_SOLUTION_REV DROP COLUMN AUTHORS;
-- 6
ALTER TABLE C_SOLUTION_REV CHANGE USER_ID OWNER_ID CHAR(36) NOT NULL;
-- 5
ALTER TABLE C_PEER_SUB CHANGE USER_ID OWNER_ID CHAR(36) NOT NULL;
-- 4
ALTER TABLE C_ARTIFACT CHANGE USER_ID OWNER_ID CHAR(36) NOT NULL;
-- 3
ALTER TABLE C_SOLUTION CHANGE USER_ID OWNER_ID CHAR(36) NOT NULL;
-- 2
ALTER TABLE C_SOLUTION DROP COLUMN PICTURE;
-- 1
ALTER TABLE C_SOLUTION ADD COLUMN PROVIDER VARCHAR(64);
