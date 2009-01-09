package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import sernet.gs.ui.rcp.main.ImageCache;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 * Root element of a tree. Used as root element in TreeViewer. All childen are
 * of type GefaehrdungsUmsetzungen.
 * 
 * @author ahanekop@sernet.de
 */
public class GefaehrdungsBaumRoot implements IGefaehrdungsBaumElement {

	private List<IGefaehrdungsBaumElement> children =
			new ArrayList<IGefaehrdungsBaumElement>();
	
	/**
	 * Constructor.
	 * 
	 * @param arrListGefaehrdungsUmsetzungen
	 *            the list of GefaehrdungsUmsetzungen to add as children of the
	 *            root element
	 */
	public GefaehrdungsBaumRoot(
			List<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen) {
		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : arrListGefaehrdungsUmsetzungen) {
			children.add(gefaehrdungsUmsetzung);
		}
	}
	
	/**
	 * Returns the description of the root element. Since the root element is
	 * not a Gefaehrdung, it does not need a description and returns an empty
	 * string.
	 * Method must be implemnted due to  IGefaehrdungsBaumElement.
	 * 
	 * @return an empty string
	 */
	public String getDescription() {
		return "";
	}

	/**
	 * Returns the list of children of the root element in the tree.
	 * All children must be of the abstract type IGefaehrdungsBaumElement.
	 * 
	 * @return the list of children of the root element
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		return children;
	}

	/**
	 * GefaehrdungsBaumRoot is already the root element of the tree.
	 * Hence, no parent is to be returned.
	 * 
	 * @return null no parent to return
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		return null;
	}

	
	/**
	 * Returns the title of the root element.
	 * 
	 * @return the title of the root element
	 */
	public String getText() {
		return "root";
	}
}
