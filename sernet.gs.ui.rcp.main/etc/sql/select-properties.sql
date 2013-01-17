SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
WHERE cnatreeelement.uuid='2915c1aa-0ae3-46b7-92bc-beeb42eabc95'