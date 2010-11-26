select bst, mn, umstxt, zo_bst, obmm.id.zobIdMit
			from ModZobjBstMass obm, 
				MUmsetzStatTxt umstxt, 
				NZielobjekt zo, 
				MbBaust bst, 
				MbMassn mn,
				ModZobjBst zo_bst,
				ModZobjBstMassMitarb obmm
			where zo.id.zobImpId = 1
			and zo.id.zobId = 1
			and umstxt.id.sprId = 1 
			and obm.ustId = umstxt.id.ustId 
			and obm.id.zobImpId = zo.id.zobImpId 
			and obm.id.zobId 	= zo.id.zobId 
			and obm.id.bauId 	= bst.id.bauId 
			and obm.id.bauImpId = bst.id.bauImpId 
			and obm.id.masId 	= mn.id.masId 
			and obm.id.masImpId = mn.id.masImpId 
			and obm.loeschDatum = null
			and obmm.id.zobId = zo.id.zobId 
			and obmm.loeschDatum = null
			
			