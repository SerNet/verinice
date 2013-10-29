select zsb, vertr.name, verf.name, integ.name 
	from 
	    NZobSb zsb,
		MSchutzbedarfkategTxt vertr,
		MSchutzbedarfkategTxt verf,
		MSchutzbedarfkategTxt integ
	where
		vertr.id.sprId = 1
		and verf.id.sprId = 1
		and integ.id.sprId = 1
		and vertr.id.sbkId = zsb.zsbVertrSbkId
		and verf.id.sbkId = zsb.zsbVerfuSbkId
		and integ.id.sbkId = zsb.zsbIntegSbkId
		and zsb.id.zobId = 10001
		
		
			
			