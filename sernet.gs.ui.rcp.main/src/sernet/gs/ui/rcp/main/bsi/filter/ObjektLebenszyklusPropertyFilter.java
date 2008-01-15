package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * 
 * @author koderman@sernet.de
 *
 */
public class ObjektLebenszyklusPropertyFilter extends StringPropertyFilter {

	public ObjektLebenszyklusPropertyFilter(StructuredViewer viewer) {
		super(viewer, "");
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof CnATreeElement)
				|| element instanceof ITVerbund
				|| !(element instanceof IBSIStrukturElement)
				|| element instanceof IBSIStrukturKategorie)
			return true;
		
		
		
		Entity entity = ((CnATreeElement)element).getEntity();
		String propertyTypeId = ((CnATreeElement)element).getTypeId() + "_status";
		String value = entity.getSimpleValue(propertyTypeId);
		Matcher matcher = regex.matcher(value);
		if (matcher.find())
			return true;
		return false;
	}

}
