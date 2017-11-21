-- Script to upgrade database used by the Common Data Service
-- FROM version 1.8.x TO version 1.9.x.
-- No database is created or specified to allow flexible deployment!

ALTER TABLE C_PEER_SUB 
ADD COLUMN OPTIONS VARCHAR(1024) CHECK (METADATA IS NULL OR JSON_VALID(OPTIONS)) AFTER SELECTOR;
