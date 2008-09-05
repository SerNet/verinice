package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Filter to select items based on search string comparison with Zielobjekt.
 * 
 * @author koderman@sernet.de
 *
 */
public class ZielobjektPropertyFilter extends StringPropertyFilter {

	public ZielobjektPropertyFilter(StructuredViewer viewer) {
		super(viewer, "");
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof MassnahmenUmsetzung))
			return true;
		
		
		
		String value = ((CnATreeElement)element).getParent().getParent().getTitel();
		Matcher matcher = super.regex.matcher(value);
		if (matcher.find())
			return true;
		return false;
	}

}
