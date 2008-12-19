package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.Iterator;
import java.util.List;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 * Helper methods to work with threats.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public abstract class GefaehrdungsUtil {

	public static void removeBySameId(
			List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			GefaehrdungsUmsetzung gefaehrdung) {
		
		if (gefaehrdung == null)
			return;
		
		boolean remove = false;
		GefaehrdungsUmsetzung gefaehrdungInList = null;
		for (Iterator iterator = allGefaehrdungsUmsetzungen.iterator(); iterator
				.hasNext();) {
			gefaehrdungInList = (GefaehrdungsUmsetzung) iterator
					.next();
			if (gefaehrdung.getId() == null || gefaehrdungInList.getId() == null)
				continue;
			if (gefaehrdungInList.getId().equals(gefaehrdung.getId()))
				remove = true;
		}

		if (remove)
			allGefaehrdungsUmsetzungen.remove(gefaehrdungInList);
	}
	
	public static GefaehrdungsUmsetzung removeBySameId(
			List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			Gefaehrdung gefaehrdung) {
		
		if (gefaehrdung == null)
			return null;
		
		boolean remove = false;
		GefaehrdungsUmsetzung gefaehrdungInList = null;
		findGefaehrdung: for (Iterator iterator = allGefaehrdungsUmsetzungen.iterator(); iterator
				.hasNext();) {
			gefaehrdungInList = (GefaehrdungsUmsetzung) iterator
					.next();
			if (gefaehrdung.getId() == null || gefaehrdungInList.getId() == null)
				continue;
			if (gefaehrdungInList.getId().equals(gefaehrdung.getId())) {
				remove = true;
				break findGefaehrdung;
			}
		}

		if (remove) {
			allGefaehrdungsUmsetzungen.remove(gefaehrdungInList);
			return gefaehrdungInList;
		}
		return null; 
	}

	@SuppressWarnings("unchecked")
	public static boolean listContainsById(List selectedArrayList,
			Gefaehrdung currentGefaehrdung) {
		for (Iterator iterator = selectedArrayList.iterator(); iterator
				.hasNext();) {
			Object object = iterator.next();
			if (object instanceof Gefaehrdung) {
				Gefaehrdung gefaehrdung = (Gefaehrdung) object;
				if (gefaehrdung.getId().equals(currentGefaehrdung.getId()))
					return true;
			}
			if (object instanceof GefaehrdungsUmsetzung ) {
				GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) object;
				if (gefaehrdung.getId().equals(currentGefaehrdung.getId()))
					return true;
			}
		}
		return false;
	}
	
	public static boolean listContainsById(List<GefaehrdungsUmsetzung> selectedArrayList,
			GefaehrdungsUmsetzung currentGefaehrdung) {
		for (GefaehrdungsUmsetzung gefaehrdung : selectedArrayList) {
			if (gefaehrdung.getId().equals(currentGefaehrdung.getId()))
				return true;
		}
		return false;
	}
	
	

}
