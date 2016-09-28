SELECT cnatreeelement.uuid,
  properties.propertytype,
  DBMS_LOB.SUBSTR( properties.propertyvalue, 100, 1 )
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
WHERE cnatreeelement.dbid = 277886
or cnatreeelement.uuid = '3fe510cc-e8d8-4e39-89db-854e1bb472ab'