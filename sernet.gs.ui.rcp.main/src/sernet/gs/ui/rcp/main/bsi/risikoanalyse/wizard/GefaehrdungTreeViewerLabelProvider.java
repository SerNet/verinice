package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Provides an image and text for each item in the TreeViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class GefaehrdungTreeViewerLabelProvider implements ILabelProvider {

	/**
	 * Returns the image of the element.
	 * 
	 * @param element the element which's image is requested
	 * @return the image of the element
	 */
	public Image getImage(Object element) {
		try {
			IGefaehrdungsBaumElement iGefaehrdungsBaumElement =
				(IGefaehrdungsBaumElement) element;
			return iGefaehrdungsBaumElement.getImage();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the text of the element
	 * 
	 * @param element the element which's text is requested
	 * @return the text of the element
	 */
	public String getText(Object element) {
		try {
			IGefaehrdungsBaumElement iGefaehrdungsBaumElement =
				(IGefaehrdungsBaumElement) element;
			return iGefaehrdungsBaumElement.getText();
		} catch (Exception e) {
			return "";
		}
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
