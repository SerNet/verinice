package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;


public class MassnahmenSiegelFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Collection<String> siegelPattern;

	public MassnahmenSiegelFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String[] getPattern() {
		return siegelPattern != null ? 
				(String[]) siegelPattern.toArray(new String[siegelPattern.size()])
				: new String[] {};
	}

	public void setPattern(String[] newPattern) {
		boolean active = siegelPattern != null;
		if (newPattern != null && newPattern.length > 0) {
			siegelPattern = new HashSet<String>();
			for (String type : newPattern) 
				siegelPattern.add(type);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		siegelPattern = null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof MassnahmenUmsetzung
				|| element instanceof Massnahme))
			return true;
		
		if (element instanceof MassnahmenUmsetzung) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
			return siegelPattern.contains(Character.toString(mn.getStufe()));
		}
		
		Massnahme mn = (Massnahme) element;
		return siegelPattern.contains(Character.toString(mn.getSiegelstufe()));
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
