--Donwgrade a a verinice database from version 1 to 0.99
UPDATE CNATREEELEMENT SET DBVERSION=.99 WHERE OBJECT_TYPE='bsimodel';
DROP TABLE ACCOUNTGROUP;