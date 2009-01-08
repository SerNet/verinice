package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;

/**
 * Default listener used inside model. Does not do anything.
 *
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class NullListener implements IBSIModelListener {

	public void childAdded(CnATreeElement category, CnATreeElement child) {
		// do nothing
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		// do nothing

	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		// do nothing

	}

	public void linkChanged(CnALink link) {
		// do nothing

	}

	public void modelRefresh() {
		// do nothing
	}

}
