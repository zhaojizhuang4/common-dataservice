-- Script to downgrade database used by the Common Data Service
-- FROM version 1.9.x TO version 1.8.x.
-- No database is created or specified to allow flexible deployment!

-- Undo: 4 add the constraint
ALTER TABLE C_SOLUTION DROP FOREIGN KEY C_SOLUTION_C_PEER;
-- Undo: 3 add the column
ALTER TABLE C_SOLUTION DROP COLUMN SOURCE_ID;
-- Undo: 2 add the constraint
ALTER TABLE C_PEER_SUB DROP FOREIGN KEY C_PEER_SUB_C_USER;
-- Undo: 1 add the column
ALTER TABLE C_PEER_SUB DROP COLUMN OWNER_ID;
