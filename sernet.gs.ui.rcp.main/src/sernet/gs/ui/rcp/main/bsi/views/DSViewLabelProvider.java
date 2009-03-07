package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.IBaseLabelProvider;

import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

public class DSViewLabelProvider extends BSIModelViewLabelProvider implements
		IBaseLabelProvider {

	public DSViewLabelProvider(TreeViewerCache cache) {
		super(cache);
	}
	
	@Override
	public String getText(Object obj) {
		if (obj instanceof ITVerbund) {
			String title = super.getText(obj);
			return "Datenschutzkonzept: " + title;
		}
		else if (obj instanceof AnwendungenKategorie)
			return "Verfahren";
		
		// else return object title:
		return super.getText(obj);
	}

}
