package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.log4j.Logger;
import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;

public class GefaehrdungTreeViewerContentProvider  
		implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		try {
			IGefaehrdungsBaumElement elmt = (IGefaehrdungsBaumElement) parentElement;
			return elmt.getGefaehrdungsBaumChildren().toArray();
			
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug("error: " + e.toString());
			return null;
		}
	}

	public Object getParent(Object element) {
		try {
			IGefaehrdungsBaumElement elmt = (IGefaehrdungsBaumElement) element;
			return elmt.getGefaehrdungsBaumParent();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug("error: " + e.toString());
			return null;
		}
	}

	public boolean hasChildren(Object element) {
		try {
			IGefaehrdungsBaumElement elmt = (IGefaehrdungsBaumElement) element;
			// TODO if-else eleganter lösen
			if (elmt.getGefaehrdungsBaumChildren() == null) {
				return false;
			} else {
				return elmt.getGefaehrdungsBaumChildren().size() > 0;
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug("error: " + e.toString());
			return false;
		}
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}
}