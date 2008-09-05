package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

public class TableViewerLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		
		if (columnIndex == 0)
		  return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
		
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		GefaehrdungsUmsetzung	 gef = (GefaehrdungsUmsetzung) element;
		switch (columnIndex) {
		case 0:
			return null;
		case 1:
			return gef.getId();
		case 2:
			return gef.getTitel();
		case 3:
			return gef.getAlternativeText();
		};
		return "";
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

}
