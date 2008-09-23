package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.Iterator;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

public abstract class GefaehrdungsUtil {

	public static void removeBySameId(
			List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			GefaehrdungsUmsetzung gefaehrdung) {
		
		if (gefaehrdung == null)
			return;
		
		boolean remove = false;
		GefaehrdungsUmsetzung gefaehrdung2 = null;
		for (Iterator iterator = allGefaehrdungsUmsetzungen.iterator(); iterator
				.hasNext();) {
			gefaehrdung2 = (GefaehrdungsUmsetzung) iterator
					.next();
			if (gefaehrdung.getId() == null || gefaehrdung2.getId() == null)
				continue;
			if (gefaehrdung2.getId().equals(gefaehrdung.getId()))
				remove = true;
		}

		if (remove)
			allGefaehrdungsUmsetzungen.remove(gefaehrdung2);
	}

}
