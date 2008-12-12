package sernet.gs.ui.rcp.main.bsi.filter;

import javax.swing.text.html.ListView;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

public class TagFilter extends ViewerFilter {
	
	public static final String NO_TAG = "[keine Tags]";

	String[] pattern;
	private StructuredViewer viewer;
	
	public TagFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IBSIStrukturElement) || (element instanceof ITVerbund))
			return true;
		
		IBSIStrukturElement zielobjekt = (IBSIStrukturElement) element;
		for (String tag : pattern) {
			if (tag.equals(NO_TAG)) {
				if (zielobjekt.getTags().size()<1)
					return true;
			}
			
			for (String zielTag : zielobjekt.getTags()) {
				if (zielTag.equals(tag))
					return true;
			}
		}
		return false;
	}

	public String[] getPattern() {
		return pattern;
	}

	public void setPattern(String[] newPattern) {
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

}
