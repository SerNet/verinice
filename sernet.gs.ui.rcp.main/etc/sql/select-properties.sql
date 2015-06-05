SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
WHERE cnatreeelement.dbid = 277886
-- cnatreeelement.uuid = ''