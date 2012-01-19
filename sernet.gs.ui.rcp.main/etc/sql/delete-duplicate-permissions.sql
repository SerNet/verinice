-- Prueft ob Eintraege mit gleicher 
-- cte_id,role,readallowed und writeallowed Spalte 
-- vorhanden sind
-- Wenn das Ergebnis nicht leer ist muss geloescht werden
SELECT MAX(dbid) FROM permission WHERE cte_id IN (
	select cte_id
	from permission 
	group by cte_id,role,readallowed,writeallowed
	HAVING  count(cte_id)>1
)

-- Loescht jeweils einen Eintrag in permissions
-- mit gleicher cte_id,role,readallowed und writeallowed Spalte
DELETE FROM permission WHERE dbid IN (
	SELECT MAX(dbid) FROM permission WHERE cte_id IN (
		select cte_id
		from permission 
		group by cte_id,role,readallowed,writeallowed
		HAVING  count(cte_id)>1
	)	
)

