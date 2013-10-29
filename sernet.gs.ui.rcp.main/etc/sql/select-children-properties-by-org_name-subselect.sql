SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
  JOIN cnatreeelement parent ON cnatreeelement.parent=parent.dbid
WHERE parent.dbid=786839368 or
parent.uuid IN (SELECT cnatreeelement.uuid
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
WHERE properties.propertytype='org_name'
AND properties.propertyvalue='Security Assessment: [dm GmbH]')