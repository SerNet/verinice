SELECT p2.PROPERTYVALUE AS GRUPPE,
  p1.PROPERTYVALUE AS NACHNAME,
  p0.PROPERTYVALUE AS LOGINNAME,
  p3.PROPERTYVALUE AS email
FROM cnatreeelement t0
  -- Parent / Gruppe
  JOIN cnatreeelement pa0 ON t0.parent=pa0.DBID
  JOIN entity e2 ON pa0.entity_id=e2.dbid
  JOIN propertylist pl2 ON pl2.typedlist_id=e2.dbid
  JOIN properties p2 ON p2.properties_id=pl2.dbid
  -- Account
  JOIN CONFIGURATION c0 ON PERSON_ID=t0.DBID
  JOIN entity e0 ON c0.entity_id=e0.dbid
  JOIN propertylist pl0 ON pl0.typedlist_id=e0.dbid
  JOIN properties p0 ON p0.properties_id=pl0.dbid
  -- E-Mail
  JOIN entity e3 ON c0.entity_id=e3.dbid
  JOIN propertylist pl3 ON pl3.typedlist_id=e3.dbid
  JOIN properties p3 ON p3.properties_id=pl3.dbid
  -- Person
  JOIN entity e1 ON t0.entity_id=e1.dbid
  JOIN propertylist pl1 ON pl1.typedlist_id=e1.dbid
  JOIN properties p1 ON p1.properties_id=pl1.dbid
WHERE p0.PROPERTYTYPE = 'configuration_benutzername'
  AND p1.propertytype = 'person-iso_surname'
  AND p3.propertytype like '%email%';  