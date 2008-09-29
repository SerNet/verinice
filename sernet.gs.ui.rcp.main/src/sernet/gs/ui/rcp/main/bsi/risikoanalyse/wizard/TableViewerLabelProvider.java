package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 * Provides text or image for each column of a given element
 * in the TableViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class TableViewerLabelProvider implements ITableLabelProvider {

	/**
	 * Provides an image in the first column of each item.
	 * 
	 * @param element the item representing the entire row
	 * @param columnIndex the zero-based index of the column
	 *        in which the label appears
	 * @return always the image for Gefaehrdungen
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		
		if (columnIndex == 0)
		  return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
		
		return null;
	}

	/**
	 * Provides the text for each column, except the first, per item.
	 * 
	 * @param element the item representing the entire row
	 * @param columnIndex the zero-based index of the column
	 *        in which the label appears
	 */
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
