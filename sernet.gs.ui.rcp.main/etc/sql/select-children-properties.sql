SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
  JOIN cnatreeelement parent ON cnatreeelement.parent=parent.dbid
WHERE parent.dbid=786839368 or
parent.uuid='4c053cad-8aff-4fd9-a55b-8db64ee0a518'

SELECT * from cnatreeelement WHERE uuid='4c053cad-8aff-4fd9-a55b-8db64ee0a518'