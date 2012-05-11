-- Role back from 0.99 to 0.98
ALTER TABLE CNATREEELEMENT DROP COLUMN SCOPE_ID;

UPDATE CNATREEELEMENT set DBVERSION=0.98 WHERE OBJECT_TYPE='bsimodel';

UPDATE properties SET propertyvalue='my-user-group'  WHERE propertyvalue LIKE 'user-default-group' AND propertytype = 'configuration_rolle';

UPDATE properties SET propertyvalue='my-admin-group'  WHERE propertyvalue LIKE 'admin-default-group' AND propertytype = 'configuration_rolle';
