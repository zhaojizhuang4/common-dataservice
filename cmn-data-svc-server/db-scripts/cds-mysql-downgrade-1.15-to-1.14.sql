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
-- FROM version 1.15.x TO version 1.14.x.
-- No database is specified to allow flexible deployment!

-- 6
ALTER TABLE C_SOLUTION
  ADD COLUMN VALIDATION_STATUS_CD CHAR(2);
-- 5
UPDATE C_SOLUTION
  SET VALIDATION_STATUS_CD = 'NV';
-- 4
ALTER TABLE C_SOLUTION_REV
  DROP COLUMN VALIDATION_STATUS_CD;
-- 3
ALTER TABLE C_SOLUTION
  ADD COLUMN ACCESS_TYPE_CD CHAR(2);
-- 2
UPDATE C_SOLUTION
  SET ACCESS_TYPE_CD = 'PR';
-- 1
ALTER TABLE C_SOLUTION_REV
  DROP COLUMN ACCESS_TYPE_CD;
