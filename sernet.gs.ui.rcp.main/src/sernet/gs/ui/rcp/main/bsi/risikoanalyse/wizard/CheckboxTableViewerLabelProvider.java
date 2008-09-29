package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 *  Provides text or image for each column per item in the CheckboxTableViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class CheckboxTableViewerLabelProvider implements ITableLabelProvider {

	/**
	 * Provides an image in the second column of each item.
	 * 
	 * @param element the item representing the entire row
	 * @param columnIndex the zero-based index of the column
	 *        in which the label appears
	 * @return always the image for Gefaehrdungen
	 */
	public Image getColumnImage(Object element, int columnIndex) {

		if (columnIndex == 1)
			return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);

		return null;
	}

	/**
	 * Provides the text for each column, except the first two, per item.
	 * 
	 * @param element the item representing the entire row
	 * @param columnIndex the zero-based index of the column
	 *        in which the label appears
	 * @return the colum's text
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof GefaehrdungsUmsetzung) {
			GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
			return getText(gef, columnIndex);
		}
		if (element instanceof Gefaehrdung) {
			Gefaehrdung gef = (Gefaehrdung) element;
			return getText(gef, columnIndex);
		}
		return "";
	}

	/**
	 * Returns the text for a given column of a Gefaehrdung.
	 * 
	 * @param gefaehrdung the Gefaehrdung to get the text for
	 * @param columnIndex the column of the CheckboxTableViewer to get
	 * 		  the text for
	 * @return the text for the given column and Gefaehrdung
	 */
	private String getText(Gefaehrdung gefaehrdung, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return null;
		case 1:
			return null;
		case 2:
			return gefaehrdung.getId();
		case 3:
			return gefaehrdung.getTitel();
		case 4:
			return gefaehrdung.getKategorieAsString();
		};
		return "";
	}

	/**
	 * Returns the text for a given column of a GefaehrdungsUmsetzung.
	 * 
	 * @param gefaehrdung the GefaehrdungsUmsetzung to get the text for
	 * @param columnIndex the column of the CheckboxTableViewer to get
	 * 		  the text for
	 * @return the text for the given column and GefaehrdungsUmsetzung
	 */
	private String getText(GefaehrdungsUmsetzung gef, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return null;
		case 1:
			return null;
		case 2:
			return gef.getId();
		case 3:
			return gef.getTitel();
		case 4:
			return gef.getKategorie();
		};
		return "";
	}

	/**
	 * Not implemented - inherited by IBaseLabelProvider.
	 * 
	 * @param listener a label provider listener
	 */
	public void addListener(ILabelProviderListener listener) {}

	/**
	 * Not implemented - inherited by IBaseLabelProvider.
	 */
	public void dispose() {}

	/**
	 * Returns whether the label would be affected 
     * by a change to the given property of the given element.
     * 
     * @param element the item representing the entire row
     * @param property the property
	 * @return always false  
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * Not implemented - inherited by IBaseLabelProvider.
	 * 
	 * @param listener a label provider listener
	 */
	public void removeListener(ILabelProviderListener listener) {}
}
