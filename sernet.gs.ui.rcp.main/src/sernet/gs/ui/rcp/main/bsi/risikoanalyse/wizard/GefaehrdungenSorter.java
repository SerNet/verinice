package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.common.model.NumericStringComparator;

/**
 * Sorts the Gefaehrdungen.
 * 
 * @author ahanekop@sernet.de
 */
public class GefaehrdungenSorter extends ViewerSorter {

	/**
	 * Compares two Gefaehrdungen and returns them sorted.
	 * Numbers are taken into account as well as characters.
	 * 
	 * @param viewer the viewer
     * @param e1 the first element
     * @param e2 the second element
     * @return a negative number if the first element is less than the 
     *  second element; the value <code>0</code> if the first element is
     *  equal to the second element; and a positive number if the first
     *  element is greater than the second element
	 */
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
