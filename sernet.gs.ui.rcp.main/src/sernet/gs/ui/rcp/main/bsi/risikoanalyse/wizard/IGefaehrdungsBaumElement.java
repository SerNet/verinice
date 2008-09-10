package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;
import org.eclipse.swt.graphics.Image;

/**
 * Interface must be implemented by elements, which can be added to a tree
 * of Gefaehrdungen.
 * Used for TreeViewer of Gefaehrdungen.
 * 
 * @author ahanekop@sernet.de
 */
public interface IGefaehrdungsBaumElement {
	
	/**
	 * Returns the description of the element.
	 * 
	 * @return the description of the element
	 */
	public String getDescription();

	/**
	 * Returns the children of the element.
	 *  
	 * @return the list of children of the element
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren();

	/**
	 * Returns the parent element.
	 * 
	 * @return the parent element
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent();

	/**
	 * Returns the image of the element.
	 * 
	 * @return the image of the element
	 */
	public Image getImage();
	// FIXME needs to be moved to GUI layer - akoderman

	/**
	 * Returns the title of the element.
	 * 
	 * @return the title of the element
	 */
	public String getText();
}
