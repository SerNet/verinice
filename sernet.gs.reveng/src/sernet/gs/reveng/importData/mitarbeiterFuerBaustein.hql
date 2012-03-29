select mitarbeiter.name, 
	from ModZobjBstMitarb obm,
		NZielobjekt mitarbeiter
	where obm.id.bauId = 1
			and obm.id.zobIdMit = mitarbeiter.id.zobId
			and obm.loeschDatum = null