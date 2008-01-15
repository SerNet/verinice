package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;


public class GefaehrdungenFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private boolean[] pattern = null;

	public GefaehrdungenFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public boolean[] getPattern() {
		return pattern;
	}

	public void setPattern(boolean[] newPattern) {
		boolean active = pattern != null;
		if (newPattern != null && newPattern.length > 0) {
			pattern = newPattern;
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
		if (element instanceof Gefaehrdung
				&& pattern[0] /*filter gefaehrdungen*/) {
			return false;
		}
		if (element instanceof Massnahme 
				&& pattern[1] /* filter massnahmen*/) {
			return false;
		}
		return true;
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
