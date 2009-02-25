package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.bsi.model.IMassnahmeUmsetzung;


public class MassnahmenUmsetzungFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Collection<String> umsetzungPattern;

	public MassnahmenUmsetzungFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String[] getUmsetzungPattern() {
		return umsetzungPattern != null ? 
				(String[]) umsetzungPattern.toArray(new String[umsetzungPattern.size()])
				: new String[] {};
	}

	public void setUmsetzungPattern(String[] newPattern) {
		boolean active = umsetzungPattern != null;
		if (newPattern != null && newPattern.length > 0) {
			umsetzungPattern = new HashSet<String>();
			for (String type : newPattern) 
				umsetzungPattern.add(type);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		umsetzungPattern = null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung))
			return true;
		
		IMassnahmeUmsetzung mn = (IMassnahmeUmsetzung) element;
		return umsetzungPattern.contains(mn.getUmsetzung());
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
