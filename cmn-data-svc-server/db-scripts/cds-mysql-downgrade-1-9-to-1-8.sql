-- Script to downgrade database used by the Common Data Service
-- FROM version 1.9.x TO version 1.8.x.
-- No database is created or specified to allow flexible deployment!

ALTER TABLE C_PEER_SUB 
DROP COLUMN OPTIONS;
