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

-- Script to upgrade database used by the Common Data Service
-- FROM version 1.14.x TO version 1.15.x.
-- No database is specified to allow flexible deployment!

-- 1
ALTER TABLE C_SOLUTION_REV
  ADD COLUMN ACCESS_TYPE_CD CHAR(2) NOT NULL DEFAULT 'PR';
-- 2
UPDATE C_SOLUTION_REV REV
  SET ACCESS_TYPE_CD = CASE
  WHEN (SELECT COUNT(*) FROM C_SOLUTION SOL
                        WHERE SOL.SOLUTION_ID = REV.SOLUTION_ID AND SOL.ACCESS_TYPE_CD IS NOT NULL) > 0
  THEN (SELECT ACCESS_TYPE_CD FROM C_SOLUTION SOL
                        WHERE SOL.SOLUTION_ID = REV.SOLUTION_ID)
  ELSE 'PR'
END;
-- 3
ALTER TABLE C_SOLUTION
  DROP COLUMN ACCESS_TYPE_CD;
-- 4
ALTER TABLE C_SOLUTION_REV
  ADD COLUMN VALIDATION_STATUS_CD CHAR(2) NOT NULL DEFAULT 'NV';
-- 5
UPDATE C_SOLUTION_REV REV
  SET VALIDATION_STATUS_CD = CASE
  WHEN (SELECT COUNT(*) FROM C_SOLUTION SOL
                        WHERE SOL.SOLUTION_ID = REV.SOLUTION_ID AND SOL.VALIDATION_STATUS_CD IS NOT NULL) > 0
  THEN (SELECT VALIDATION_STATUS_CD FROM C_SOLUTION SOL
                        WHERE SOL.SOLUTION_ID = REV.SOLUTION_ID)
  ELSE 'NV'
END;
-- 6
ALTER TABLE C_SOLUTION
  DROP COLUMN VALIDATION_STATUS_CD;
-- 7
UPDATE C_SITE_CONFIG set CONFIG_VAL = 
  '{"fields":[ {"type":"text","name":"siteInstanceName","label":"siteInstanceName","required":"true","data":"Acumos"}, {"type":"file","name":"headerLogo","label":"Headerlogo","data":{"lastModified":1510831880727,"lastModifiedDate":"2017-11-16T11:31:20.727Z","name":"acumos_logo_white.png","size":3657,"type":"image/png"}}, {"type":"file","name":"footerLogo","label":"Footerlogo","data":{"lastModified":1510831874776,"lastModifiedDate":"2017-11-16T11:31:14.776Z","name":"footer_logo.png","size":3127,"type":"image/png"}}, {"type":"heading","name":"ConnectionConfig","label":"ConnectionConfig","required":"true","subFields":[ {"type":"text","name":"socketTimeout","label":"socketTimeout","required":"true","data":"300"}, {"type":"text","name":"connectionTimeout","label":"connectionTimeout","required":"true","data":"10"} ]}, {"type":"select","name":"enableOnBoarding","label":"EnableOnboarding","options":[{"name":"Enabled"},{"name":"Disabled"}],"required":true,"data":{"name":"Enabled"}}, {"type":"textarea","name":"validationText","label":"validationText","required":"false","data":"test"}, {"type":"select","name":"EnableDCAE","label":"EnableDCAE","options":[{"name":"Enabled"},{"name":"Disabled"}],"required":true,"data":{"name":"Enabled"}}]}'
  WHERE CONFIG_KEY='site_config';
