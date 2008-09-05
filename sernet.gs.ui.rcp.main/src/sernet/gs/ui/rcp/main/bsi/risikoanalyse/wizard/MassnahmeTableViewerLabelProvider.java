package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

public class MassnahmeTableViewerLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		
		if (columnIndex == 0)
			if (element instanceof RisikoMassnahmenUmsetzung) {
				return ((RisikoMassnahmenUmsetzung) element).getImage();
			} else {
				return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
			}
		
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		
		if (element instanceof RisikoMassnahmenUmsetzung) {
			RisikoMassnahmenUmsetzung massnahme = (RisikoMassnahmenUmsetzung) element;
			switch (columnIndex) {
			case 0:
				return null;
			case 1:
				return massnahme.getNumber();
			case 2:
				return "[" + massnahme.getSiegel() + "] " + massnahme.getTitel();
			case 3:
				return massnahme.getDescription();
			};
		} else {
			MassnahmenUmsetzung massnahme = (MassnahmenUmsetzung) element;
			switch (columnIndex) {
			case 0:
				return null;
			case 1:
				return massnahme.getKapitel();
			case 2:
				return "[" + massnahme.getStufe() + "] " + massnahme.getName();
			case 3:
				return "keine Beschreibung";
			};
		}
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
