package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.NumericStringComparator;

/**
 * Sorts Massnahmen in table viewer.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MassnahmenSorter extends ViewerSorter {

	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof Massnahme
				&& e2 instanceof Massnahme) {
			// sort chapters correctly by converting 2.45, 2.221, 3.42
			// to 2045, 2221, 3024
			
			return (Integer.valueOf(((Massnahme) e1).getKapitelValue())
				   .compareTo( ((Massnahme) e2).getKapitelValue()));
		}

		if (e1 instanceof MassnahmenUmsetzung
				&& e2 instanceof MassnahmenUmsetzung) {
			// sort chapters correctly by converting 2.45, 2.221, 3.42
			// to 2045, 2221, 3024
			int[] kap1 = ((MassnahmenUmsetzung) e1).getKapitelValue();
			int[] kap2 = ((MassnahmenUmsetzung) e2).getKapitelValue();
			return (Integer.valueOf(kap1[0] * 1000 + kap1[1])
					.compareTo((kap2[0] * 1000 + kap2[1])));
		}
		
		// else, consider elements as equal:
		return 0;
		
	}
}
