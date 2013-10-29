--Testet, ob mehrere identische permissions existieren
select cte_id,count(cte_id) as n
from permission 
group by cte_id,role
HAVING  count(cte_id)>1

--Zeigt permission an
SELECT * FROM permission WHERE cte_id=5268;

--Zeigt alle Properties eines Elements an
SELECT cnatreeelement.uuid,
  properties.propertytype,
  properties.propertyvalue 
FROM cnatreeelement
  JOIN entity ON cnatreeelement.entity_id=entity.dbid
  JOIN propertylist ON propertylist.typedlist_id=entity.dbid
  JOIN properties ON properties.properties_id=propertylist.dbid
WHERE cnatreeelement.dbid=5268