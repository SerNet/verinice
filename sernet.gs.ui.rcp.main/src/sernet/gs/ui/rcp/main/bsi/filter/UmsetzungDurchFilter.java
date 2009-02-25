package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.bsi.model.IMassnahmeUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;


/**
 * Reusable filter to select items based on property values.
 * 
 * @author koderman@sernet.de
 *
 */
public class UmsetzungDurchFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private String pattern=null;
	protected String propertyType;
	protected Pattern regex;

	/**
	 * 
	 * @param viewer
	 * @param type
	 */
	public UmsetzungDurchFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String getPattern() {
		return pattern;
	}
	
	public void setPattern(String newPattern) {
		boolean active = pattern != null;
		if (newPattern != null && newPattern.length() > 0) {
			pattern = newPattern;
			regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
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
		regex=null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung))
			return true;
		
		IMassnahmeUmsetzung mnums = (IMassnahmeUmsetzung)element;
		Matcher matcher = regex.matcher(mnums.getUmsetzungDurch());
		if (matcher.find())
			return true;
		return false;
	}

	

//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
