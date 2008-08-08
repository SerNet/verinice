package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import sernet.gs.ui.rcp.main.ImageCache;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

public class GefaehrdungsBaumRoot implements IGefaehrdungsBaumElement {

	private List<IGefaehrdungsBaumElement> children = new ArrayList<IGefaehrdungsBaumElement>();
	private Image image = ImageCache.getInstance().getImage(ImageCache.BAUSTEIN);

	public GefaehrdungsBaumRoot(
			ArrayList<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen) {
		Logger.getLogger(this.getClass()).debug("root - constructor");
		/* convert from ArrayList<GefaehrdungsUmsetzung> into List<IGefaehrdungsBaumElement> */
		// TODO gibt's hier eine elegantere Methode?
		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : arrListGefaehrdungsUmsetzungen) {
			children.add(gefaehrdungsUmsetzung);
		}
	}

	/**
	 * returns the List of children in the tree. The children are of Type
	 * IGefaehrdungsBaumElement, originally of Type
	 * RisikoMassnahmenUmsetzungen.
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		Logger.getLogger(this.getClass()).debug("root - getGefaehrdungsBaumChildren");
		return children;
	}

	/**
	 * GefaehrdungsBaumRoot is already root of tree.
	 * No parent to return.
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		Logger.getLogger(this.getClass()).debug("root - getGefaehrdungsBaumParent");
		return null;
	}

	/**
	 *  returns the image for the root element of viewer
	 */
	public Image getImage() {
		Logger.getLogger(this.getClass()).debug("root - getImage");
		return image;
	}

	/**
	 *   returns the text for the root element of viewer
	 */
	public String getText() {
		Logger.getLogger(this.getClass()).debug("root - getText");
		return "root";
	}

}
