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
-- FROM version 1.18.x TO version 1.17.x.
-- No database is specified to allow flexible deployment!


-- 6
DELETE FROM C_USER_ROLE_MAP WHERE ROLE_ID = '9d961018-5464-5b0e-a9c2-11dcdfdb67a0';
DELETE FROM C_ROLE WHERE ROLE_ID = '9d961018-5464-5b0e-a9c2-11dcdfdb67a0' and NAME = 'Publisher';
-- 5
ALTER TABLE C_SOLUTION_WEB CHANGE LAST_DOWNLOAD LAST_DOWNLOAD TIMESTAMP;
-- 4 
ALTER TABLE C_USER_LOGIN_PROVIDER CHANGE EXPIRE_TIME EXPIRE_TIME TIMESTAMP;
-- 3
ALTER TABLE C_PEER_SUB CHANGE PROCESSED_DATE PROCESSED_DATE TIMESTAMP;
-- 2
DROP TABLE C_PUBLISH_REQUEST;
-- 1
DROP TABLE C_USER_TAG_MAP;
