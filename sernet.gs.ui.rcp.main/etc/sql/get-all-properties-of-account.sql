--- Retrieve all properties of account named 'accountname'
SELECT entity.dbid, p1.*
FROM entity 
  JOIN propertylist pl0 ON pl0.typedlist_id=entity.dbid
  JOIN properties p0 ON p0.properties_id=pl0.dbid
  JOIN propertylist pl1 ON pl1.typedlist_id=entity.dbid
  JOIN properties p1 ON p1.properties_id=pl1.dbid
WHERE p0.propertytype = 'configuration_benutzername'
AND p0.propertyvalue = 'accountname';