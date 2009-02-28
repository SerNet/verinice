package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
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

	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		// do nothing
	}

	public void linkRemoved(CnALink link) {
		// do nothing
		
	}
	
	public void linkAdded(CnALink link) {
		// do nothing
	}

	public void databaseChildAdded(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildChanged(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildRemoved(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void modelReload(BSIModel newModel) {
		// TODO Auto-generated method stub
		
	}

}
