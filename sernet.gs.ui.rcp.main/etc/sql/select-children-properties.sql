SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
  JOIN cnatreeelement parent ON cnatreeelement.parent=parent.dbid
WHERE parent.uuid='27f1e244-4971-4645-b35d-d6541563add7'