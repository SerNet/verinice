package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

/**
 * 
 * Filters controls based on level.
 * 
 * @author koderman@sernet.de
 *
 */
public class BSISchichtFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Collection<String> pattern;

	public BSISchichtFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String[] getPattern() {
		return pattern != null ? 
				(String[]) pattern.toArray(new String[pattern.size()])
				: new String[] {};
	}
	

	public void setPattern(String[] newPattern) {
		boolean active = pattern != null;
		if (newPattern != null && newPattern.length > 0) {
			pattern = new HashSet<String>();
			for (String type : newPattern) 
				pattern.add(type);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		pattern = null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof Baustein))
			return true;

		Baustein bs = (Baustein) element;
		return pattern.contains(Integer.toString(bs.getSchicht()));
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
