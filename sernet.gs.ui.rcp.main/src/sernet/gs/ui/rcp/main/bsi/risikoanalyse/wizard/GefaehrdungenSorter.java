package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.common.model.NumericStringComparator;

/**
 * 
 * @author ahanekop@sernet.de
 *
 */
public class GefaehrdungenSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof Gefaehrdung
				&& e2 instanceof Gefaehrdung))
			return 0;
		
		Gefaehrdung gef1 = (Gefaehrdung) e1;
		Gefaehrdung gef2 = (Gefaehrdung) e2;
		
		NumericStringComparator numComp = new NumericStringComparator();
		return numComp.compare(gef1.getId(), gef2.getId());
	}
}
