package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.IMassnahmeUmsetzung;

public class AuditDurchFilter extends UmsetzungDurchFilter {

	public AuditDurchFilter(StructuredViewer viewer) {
		super(viewer);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung))
			return true;
		
		IMassnahmeUmsetzung mnums = (IMassnahmeUmsetzung)element;
		Matcher matcher = regex.matcher(mnums.getRevisionDurch());
		if (matcher.find())
			return true;
		return false;
	}
}
