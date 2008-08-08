/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 * @author ahanekop@sernet.de
 *
 */
public class GefaehrdungTreeViewerLabelProvider implements ILabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Logger.getLogger(this.getClass()).debug("label - getImage");
		try {
			IGefaehrdungsBaumElement iGefaehrdungsBaumElement = (IGefaehrdungsBaumElement) element;
			return iGefaehrdungsBaumElement.getImage();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		Logger.getLogger(this.getClass()).debug("label - getText");
		try {
			IGefaehrdungsBaumElement iGefaehrdungsBaumElement = (IGefaehrdungsBaumElement) element;
			return iGefaehrdungsBaumElement.getText();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		Logger.getLogger(this.getClass()).debug("label - addListener");
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		Logger.getLogger(this.getClass()).debug("label - dispose");
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		Logger.getLogger(this.getClass()).debug("label - isLabelProperty");
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		Logger.getLogger(this.getClass()).debug("label - removeListener");
		// TODO Auto-generated method stub
	}

}
