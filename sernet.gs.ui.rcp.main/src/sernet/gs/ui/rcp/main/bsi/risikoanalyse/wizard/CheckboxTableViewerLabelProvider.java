package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;

/**
 *  Provides text or image for each column per item in the CheckboxTableViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class CheckboxTableViewerLabelProvider implements ITableLabelProvider {

	/**
	 * Provides an image in the first column of each item.
	 * 
	 * @param element
	 *            the item representing the entire row
	 * @param columnIndex
	 *            the zero-based index of the column in which the label appears
	 * @return always the image for Gefaehrdungen
	 */
	public Image getColumnImage(Object element, int columnIndex) {

		if (columnIndex == 1)
			return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);

		return null;
	}

	/**
	 * Provides the text for each column, except the first row, per item.
	 * 
	 * @param element
	 *            the item representing the entire row
	 * @param columnIndex
	 *            the zero-based index of the column in which the label appears
	 */
	public String getColumnText(Object element, int columnIndex) {
		
		Gefaehrdung gef = (Gefaehrdung) element;
		
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
			return gef.getKategorieAsString();
		};
		return "";
	}

	/**
	 * Not implemented - inherited method.
	 * 
	 * @param listener a label provider listener
	 */
	public void addListener(ILabelProviderListener listener) {}

	/**
	 * Not implemented - inherited method.
	 */
	public void dispose() {}

	/**
	 * Returns whether the label would be affected 
     * by a change to the given property of the given element.
     * 
     * @param element
	 *            the item representing the entire row
	 * @param columnIndex
	 *            the zero-based index of the column in which the label appears
	 * @return always false            
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * Not implemented - inherited method.
	 */
	public void removeListener(ILabelProviderListener listener) {}
}