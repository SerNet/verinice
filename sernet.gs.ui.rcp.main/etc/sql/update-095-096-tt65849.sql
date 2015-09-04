
UPDATE cnatreeelement set object_type='gebaeude' WHERE cnatreeelement.dbid IN (SELECT dbid from gebaeude);
UPDATE cnatreeelement set object_type='person' WHERE cnatreeelement.dbid IN (SELECT dbid from person);
UPDATE cnatreeelement set object_type='baustein-umsetzung' WHERE cnatreeelement.dbid IN (SELECT dbid from bausteinumsetzung);
UPDATE cnatreeelement set object_type='massnahmen-umsetzung' WHERE cnatreeelement.dbid IN (SELECT dbid from massnahmenumsetzung);
UPDATE cnatreeelement set object_type='risiko-massnahmen-umsetzung' WHERE cnatreeelement.dbid IN (SELECT dbid from risikomassnahmenumsetzung);
UPDATE cnatreeelement set object_type='gefaehrdungs-umsetzung' WHERE cnatreeelement.dbid IN (SELECT dbid from gefaehrdungsumsetzung);
UPDATE cnatreeelement set object_type='finished-risk-analysis' WHERE cnatreeelement.dbid IN (SELECT dbid from finishedriskanalysis);
UPDATE cnatreeelement set object_type='anwendung' WHERE cnatreeelement.dbid IN (SELECT dbid from anwendung);
UPDATE cnatreeelement set object_type='client' WHERE cnatreeelement.dbid IN (SELECT dbid from client);
UPDATE cnatreeelement set object_type='sonst-it' WHERE cnatreeelement.dbid IN (SELECT dbid from sonstit);
UPDATE cnatreeelement set object_type='netz-komponente' WHERE cnatreeelement.dbid IN (SELECT dbid from netzkomponente);
UPDATE cnatreeelement set object_type='raum' WHERE cnatreeelement.dbid IN (SELECT dbid from raum);
UPDATE cnatreeelement set object_type='telefon-komponente' WHERE cnatreeelement.dbid IN (SELECT dbid from telefonkomponente);
UPDATE cnatreeelement set object_type='server' WHERE cnatreeelement.dbid IN (SELECT dbid from server);
UPDATE cnatreeelement set object_type='gebaeude-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from gebaeudekategorie);
UPDATE cnatreeelement set object_type='anwendungen-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from anwendungenkategorie);
UPDATE cnatreeelement set object_type='clients-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from clientskategorie);
UPDATE cnatreeelement set object_type='sonstige-it-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from sonstigeitkategorie);
UPDATE cnatreeelement set object_type='nk-kategorie'WHERE cnatreeelement.dbid IN (SELECT dbid from nkkategorie);
UPDATE cnatreeelement set object_type='personen-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from personenkategorie);
UPDATE cnatreeelement set object_type='raeume-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from raeumekategorie);
UPDATE cnatreeelement set object_type='server-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from serverkategorie);
UPDATE cnatreeelement set object_type='tk-kategorie' WHERE cnatreeelement.dbid IN (SELECT dbid from tkkategorie);
UPDATE cnatreeelement set object_type='bsimodel'WHERE cnatreeelement.dbid IN (SELECT dbid from bsimodel);
UPDATE cnatreeelement set object_type='it-verbund' WHERE cnatreeelement.dbid IN (SELECT dbid from itverbund);
UPDATE cnatreeelement set object_type='verarbeitungsangaben' WHERE cnatreeelement.dbid IN (SELECT dbid from verarbeitungsangaben);
UPDATE cnatreeelement set object_type='verantwortliche-stelle' WHERE cnatreeelement.dbid IN (SELECT dbid from verantwortlichestelle);
UPDATE cnatreeelement set object_type='personengruppen' WHERE cnatreeelement.dbid IN (SELECT dbid from personengruppen);
UPDATE cnatreeelement set object_type='datenverarbeitung' WHERE cnatreeelement.dbid IN (SELECT dbid from datenverarbeitung);
UPDATE cnatreeelement set object_type='stellungnahme-dsb' WHERE cnatreeelement.dbid IN (SELECT dbid from stellungnahmedsb);

ALTER TABLE properties DROP CONSTRAINT fkc8cd8d337b233bb1;
ALTER TABLE properties DROP COLUMN parent;

DROP TABLE anwendung;

DROP TABLE anwendungenkategorie;

DROP TABLE bausteinumsetzung;

DROP TABLE bsimodel;

DROP TABLE client;

DROP TABLE clientskategorie;

DROP TABLE datenverarbeitung;

DROP TABLE finishedriskanalysis;

DROP TABLE gebaeude;

DROP TABLE gebaeudekategorie;

ALTER TABLE allgefaehrdungsumsetzungen DROP CONSTRAINT fka34df65360f2f8b4;
ALTER TABLE allgefaehrdungsumsetzungen ADD CONSTRAINT agu_cnatreeelement FOREIGN KEY (elt) REFERENCES cnatreeelement (dbid);
ALTER TABLE associatedgefaehrdungen DROP CONSTRAINT fk44cc7bc360f2f8b4;
ALTER TABLE allgefaehrdungsumsetzungen ADD CONSTRAINT ag_cnatreeelement FOREIGN KEY (elt) REFERENCES cnatreeelement (dbid);
ALTER TABLE notokgefaehrdungsumsetzungen DROP CONSTRAINT fka4ba6c4560f2f8b4;
ALTER TABLE allgefaehrdungsumsetzungen ADD CONSTRAINT ngu_cnatreeelement FOREIGN KEY (elt) REFERENCES cnatreeelement (dbid);


ALTER TABLE ALLGEFAEHRDUNGSUMSETZUNGEN DROP CONSTRAINT SQL100811204527181;
ALTER TABLE ASSOCIATEDGEFAEHRDUNGEN DROP CONSTRAINT SQL100811204527231;
ALTER TABLE NOTOKGEFAEHRDUNGSUMSETZUNGEN DROP CONSTRAINT SQL100811204527450;
DROP TABLE gefaehrdungsumsetzung;

DROP TABLE itverbund;

DROP TABLE risikomassnahmenumsetzung;

DROP TABLE massnahmenumsetzung;

DROP TABLE netzkomponente;

DROP TABLE nkkategorie;

ALTER TABLE configuration DROP CONSTRAINT fka2d2a4d688446992;
ALTER TABLE configuration ADD CONSTRAINT configuration_cnatreeelement FOREIGN KEY (person_id) REFERENCES cnatreeelement (dbid);

ALTER TABLE CONFIGURATION DROP CONSTRAINT SQL100811204527320;
DROP TABLE person;

DROP TABLE personengruppen;

DROP TABLE personenkategorie;

DROP TABLE raeumekategorie;

DROP TABLE raum;

DROP TABLE server;

DROP TABLE serverkategorie;

DROP TABLE sonstigeitkategorie;

DROP TABLE sonstit;

DROP TABLE stellungnahmedsb;

DROP TABLE telefonkomponente;

DROP TABLE tkkategorie;

DROP TABLE verantwortlichestelle;

DROP TABLE verarbeitungsangaben;

UPDATE cnatreeelement SET dbversion=0.96 WHERE object_type='bsimodel';