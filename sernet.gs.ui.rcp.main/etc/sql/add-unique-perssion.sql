-- Add unique constraint fuer Tabelle permission
ALTER TABLE permission ADD CONSTRAINT uc_permission UNIQUE (role,readallowed,writeallowed,cte_id);