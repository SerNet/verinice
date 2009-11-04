ALTER TABLE public.cnatreeelement ADD dbversion FLOAT8;

ALTER TABLE public.cnatreeelement ADD object_type VARCHAR(255);

ALTER TABLE public.configuration DROP CONSTRAINT fka2d2a4d688446992;

ALTER TABLE public.notokgefaehrdungsumsetzungen DROP CONSTRAINT fka4ba6c4560f2f8b4;

ALTER TABLE public.propertylist DROP CONSTRAINT fkcc839bf390ff03b9;

ALTER TABLE public.risikomassnahmenumsetzung DROP CONSTRAINT fkcfa6da189409cc0a;


ALTER TABLE public.verarbeitungsangaben DROP CONSTRAINT fk7629b59d84cb408c;


ALTER TABLE public.properties DROP COLUMN parent;


DROP TABLE public.anwendung;


DROP TABLE public.anwendungenkategorie;


DROP TABLE public.bausteinumsetzung;


DROP TABLE public.bsimodel;


DROP TABLE public.client;


DROP TABLE public.clientskategorie;


DROP TABLE public.datenverarbeitung;


DROP TABLE public.dbcopytest;


DROP TABLE public.finishedriskanalysis;


DROP TABLE public.gebaeude;


DROP TABLE public.gebaeudekategorie;


DROP TABLE public.gefaehrdungsumsetzung;


DROP TABLE public.itverbund;


DROP TABLE public.massnahmenumsetzung;


DROP TABLE public.netzkomponente;


DROP TABLE public.nkkategorie;


DROP TABLE public.person;


DROP TABLE public.personengruppen;


DROP TABLE public.personenkategorie;


DROP TABLE public.raeumekategorie;


DROP TABLE public.raum;


DROP TABLE public.risikomassnahmenumsetzung;


DROP TABLE public.server;


DROP TABLE public.serverkategorie;


DROP TABLE public.sonstigeitkategorie;


DROP TABLE public.sonstit;


DROP TABLE public.stellungnahmedsb;


DROP TABLE public.telefonkomponente;


DROP TABLE public.tkkategorie;


DROP TABLE public.verantwortlichestelle;


DROP TABLE public.verarbeitungsangaben;



