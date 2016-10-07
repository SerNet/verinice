select p1.propertyvalue AS USERNAME, p2.propertyvalue AS GROUP FROM properties p1
JOIN propertylist pl1 ON p1.properties_id=pl1.dbid
JOIN entity e1 ON pl1.typedlist_id=e1.dbid
JOIN propertylist pl2 ON e1.dbid=pl2.typedlist_id
JOIN properties p2 ON pl2.dbid=p2.properties_id
where p1.propertytype = 'configuration_benutzername' and p2.propertytype = 'configuration_rolle'
