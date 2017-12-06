-- Script to downgrade database used by the Common Data Service
-- FROM version 1.10.x TO version 1.9.x.
-- No database is created or specified to allow flexible deployment!

ALTER TABLE C_SOLUTION_VALIDATION MODIFY DETAIL VARCHAR(1024);

DROP TABLE C_COMMENT;

DROP TABLE C_THREAD;
