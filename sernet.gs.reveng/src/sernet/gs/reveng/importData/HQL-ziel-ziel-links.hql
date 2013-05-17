select dependant
	from NZielobjZielobj link,
		NZielobjekt dependant
	where link.id.zobId1 = :zobId
	and link.id.zobId2 = dependant.id.zobId
	and link.loeschDatum = null 
			
			