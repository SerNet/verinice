 select zo, txt.name, subtxt.name 
						from NZielobjekt zo, MbZielobjTypTxt txt, MbZielobjSubtypTxt subtxt 
						where zo.mbZielobjSubtyp.id.zotId = txt.id.zotId 
						and txt.id.sprId = 1 
						and zo.mbZielobjSubtyp.id.zosId = subtxt.id.zosId 
						and subtxt.id.sprId = 1
						and zo.loeschDatum = null