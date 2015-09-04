SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
WHERE cnatreeelement.dbid = 277886
or cnatreeelement.uuid = '3aef3ce3-ade1-415e-945d-610121ec41f8'