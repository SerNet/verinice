package sernet.gs.ui.rcp.main.bsi.wizards;

import org.eclipse.jface.viewers.LabelProvider;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.IEntityElement;

/**
 * 
 * @author koderman@sernet.de
 *
 */
public class ChoosePropertiesLabelProvider extends LabelProvider  {

	@Override
	public String getText(Object element) {
		if (element instanceof IEntityElement) {
			return ((IEntityElement)element).getName();
		}
		if (element instanceof EntityType) {
			return ((EntityType)element).getName();
		}
		return "";

	}


}
