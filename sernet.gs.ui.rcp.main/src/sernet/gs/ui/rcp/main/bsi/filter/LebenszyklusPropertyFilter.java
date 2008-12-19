package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * 
 * @author koderman@sernet.de
 *
 */
public class LebenszyklusPropertyFilter extends StringPropertyFilter {

	public LebenszyklusPropertyFilter(StructuredViewer viewer) {
		super(viewer, MassnahmenUmsetzung.P_LEBENSZYKLUS);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof MassnahmenUmsetzung))
			return true;
		
		Entity entity = ((CnATreeElement)element).getEntity();
		String value = entity.getSimpleValue(this.propertyType);
		Matcher matcher = regex.matcher(value);
		if (matcher.find())
			return true;
		return false;
	}

}
