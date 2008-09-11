package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * Provides an image or text for each column per item in the TableViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class MassnahmeTableViewerLabelProvider implements ITableLabelProvider {

	/**
	 * Returns the image of the element for the given column.
	 * 
	 * @param element the element representing the row
	 * @param columnIndex zero-based index of the column
	 * @return the image of the element if it's the first column, null else
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		
		if (columnIndex == 0) {
			if (element instanceof RisikoMassnahmenUmsetzung) {
				return ((RisikoMassnahmenUmsetzung) element).getImage();
			} else {
				return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns the text of the element for the given column.
	 * 
	 * @param element the element representing the row
	 * @param columnIndex zero-based index of the column
	 * @return the element's text for the column, empty string else
	 */
	public String getColumnText(Object element, int columnIndex) {
		
		if (element instanceof RisikoMassnahmenUmsetzung) {
			RisikoMassnahmenUmsetzung massnahme = (RisikoMassnahmenUmsetzung) element;
			switch (columnIndex) {
			case 0:
				return null;
			case 1:
				return massnahme.getNumber();
			case 2:
				return "[" + massnahme.getStufe() + "] " + massnahme.getTitel();
			case 3:
				return shorten(massnahme.getDescription());
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

	/**
	 * Shorten description for single-line table display
	 * 
	 * @param description The full length description.
	 * @return shortened version of the description qithout newline-characters
	 */
	private String shorten(String description) {
		String oneline = description.replaceAll("\\n", " ");
		if (oneline.length() > 100)
			return oneline.substring(0, 100) + "...";
		return oneline;
		
	}

	/**
	 * Not used.
	 * Must be implemented due to IBaseLabelProvider.
	 * 
	 * @param listener a label provider listener
	 */
	public void addListener(ILabelProviderListener listener) {}

	/**
	 * Not used.
	 * Must be implemented due to IBaseLabelProvider.
	 */
	public void dispose() {}

	/**
	 * Returns whether the label would be affected 
     * by a change to the given property of the given element.
     * 
     * @param element the element
     * @param property the property
     * @return always false
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * Not used.
	 * Must be implemented due to IBaseLabelProvider.
	 * 
	 * @param listener a label provider listener
	 */
	public void removeListener(ILabelProviderListener listener) {}
}
