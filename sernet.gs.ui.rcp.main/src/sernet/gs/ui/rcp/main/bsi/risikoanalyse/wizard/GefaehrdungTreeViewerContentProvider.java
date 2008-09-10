package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Tells the TreeViewer how to transform a domain object into an element
 * of the tree. 
 * 
 * @author ahanekop@sernet.de
 */
public class GefaehrdungTreeViewerContentProvider implements
		ITreeContentProvider {

	/**
	 * Returns the child elements of the given parent element.
	 * 
	 * @param parentElement the parent element
	 * @return an array containing the child elements of the given parent
	 */
	public Object[] getChildren(Object parentElement) {
		try {
			IGefaehrdungsBaumElement elmt = (IGefaehrdungsBaumElement) parentElement;
			return elmt.getGefaehrdungsBaumChildren().toArray();

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the parent element for the given element.
	 * 
	 * @param element the element which's parent is requested
	 * @return the parent element
	 */
	public Object getParent(Object element) {
		try {
			IGefaehrdungsBaumElement elmt = (IGefaehrdungsBaumElement) element;
			return elmt.getGefaehrdungsBaumParent();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns whether the given element has children.
	 * 
	 * @param element the element to check for children
	 * @return true if element has children, false else
	 */
	public boolean hasChildren(Object element) {
		try {
			IGefaehrdungsBaumElement elmt = (IGefaehrdungsBaumElement) element;
			// TODO if-else eleganter lösen
			if (elmt.getGefaehrdungsBaumChildren() == null) {
				return false;
			} else {
				return elmt.getGefaehrdungsBaumChildren().size() > 0;
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug("error: " + e.toString());
			return false;
		}
	}
	
	/**
     * Returns the elements to display in the viewer 
     * when its input is set to the given element. 
     *
     * @param inputElement the input element
     * @return the array of elements to display in the viewer
     */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * Not used.
	 * Must be implemented due to IContentProvider.
	 */
	public void dispose() {}

	/**
	 * Not used.
	 * Must be implemented due to IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
}