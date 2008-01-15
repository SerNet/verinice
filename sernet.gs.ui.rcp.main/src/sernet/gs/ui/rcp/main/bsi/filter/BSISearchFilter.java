package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;

public class BSISearchFilter extends ViewerFilter {

	private Pattern regex;
	private String suche;
	private StructuredViewer viewer;

	
	
	public BSISearchFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (regex==null)
			return true;
		
		if (element instanceof Baustein) {
			Baustein bs = (Baustein) element;
			Matcher matcher = regex.matcher(bs.getTitel());
			if (matcher.find())
				return true;
			// show baustein if one child matches:
			return checkMassnahmen(bs) || checkGefaehrdungen(bs);
		}
		
		if (element instanceof Massnahme) {
			Massnahme mn = (Massnahme) element;
			Matcher matcher = regex.matcher(mn.getTitel());
			return (matcher.find());
		}
		
		if (element instanceof Gefaehrdung) {
			Gefaehrdung gef = (Gefaehrdung) element;
			Matcher matcher = regex.matcher(gef.getTitel());
			return (matcher.find());
		}
		
		return false;
	}
	
	private boolean checkMassnahmen(Baustein bs) {
		List<Massnahme> massnahmen = bs.getMassnahmen();
		for (Massnahme mn : massnahmen) {
			Matcher matcher = regex.matcher(mn.getTitel());
			if (matcher.find())
				return true;
		}
		return false;
	}

	private boolean checkGefaehrdungen(Baustein bs) {
		List<Gefaehrdung> gefaehrdungen= bs.getGefaehrdungen();
		for (Gefaehrdung gefaehrdung : gefaehrdungen) {
			Matcher m = regex.matcher(gefaehrdung.getTitel());
			if (m.find())
				return true;
		}
		return false;
	}
	
	public String getPattern() {
		return suche;
	}
	
	public void setPattern(String newPattern) {
		boolean active = suche != null;
		if (newPattern != null && newPattern.length() > 0) {
			suche = newPattern;
			regex = Pattern.compile(suche, Pattern.CASE_INSENSITIVE);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		suche = null;
		regex=null;
		if (active)
			viewer.removeFilter(this);
	}

}
