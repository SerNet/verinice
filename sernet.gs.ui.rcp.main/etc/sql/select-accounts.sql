SELECT p0.parent as pp,
  t0.parent as tp,
  t0.uuid,
  p0.PROPERTIES_ID,
  DBMS_LOB.SUBSTR( p0.PROPERTYVALUE, 100, 1 ) AS LOGINNAME,
  DBMS_LOB.SUBSTR( p1.PROPERTYVALUE, 100, 1 ) AS NACHNAME,
  DBMS_LOB.SUBSTR( p2.PROPERTYVALUE, 100, 1 ) AS GRUPPE
FROM cnatreeelement t0
  JOIN cnatreeelement p0 ON t0.parent=p0.DBID
  JOIN entity e2 ON p0.entity_id=e2.dbid
  JOIN propertylist pl2 ON pl2.typedlist_id=e2.dbid
  JOIN properties p2 ON p2.properties_id=pl2.dbid
  JOIN CONFIGURATION c0 ON PERSON_ID=t0.DBID
  JOIN entity e0 ON c0.entity_id=e0.dbid
  JOIN propertylist pl0 ON pl0.typedlist_id=e0.dbid
  JOIN properties p0 ON p0.properties_id=pl0.dbid
  JOIN entity e1 ON t0.entity_id=e1.dbid
  JOIN propertylist pl1 ON pl1.typedlist_id=e1.dbid
  JOIN properties p1 ON p1.properties_id=pl1.dbid
WHERE p0.PROPERTYTYPE = 'configuration_benutzername'
  AND p1.propertytype = 'person-iso_surname';