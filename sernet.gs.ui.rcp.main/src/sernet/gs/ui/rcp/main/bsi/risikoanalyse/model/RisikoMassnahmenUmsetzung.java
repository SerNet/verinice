/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.IGefaehrdungsBaumElement;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RisikoanalyseWizard;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.wizards.ChooseExportMethodPage;
import sernet.gs.ui.rcp.main.bsi.wizards.ChoosePropertiesPage;
import sernet.gs.ui.rcp.main.bsi.wizards.ChooseReportPage;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.PropertiesRow;
import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.main.reports.TextReport;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.gs.ui.rcp.office.OOWrapper;
import sernet.hui.common.connect.EntityType;
import sernet.snutils.ExceptionHandlerFactory;


/**
 * @author ahanekop@sernet.de
 *
 */
public class RisikoMassnahmenUmsetzung extends MassnahmenUmsetzung implements IGefaehrdungsBaumElement {
	
	private GefaehrdungsUmsetzung parent;
	private Image image = ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
	
	public RisikoMassnahmenUmsetzung(CnATreeElement superParent, GefaehrdungsUmsetzung myParent) {
		super(superParent);
		this.parent = myParent;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung#getTitle()
	 */
	@Override
	public String getTitle() {
		return super.getTitle();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung#setName(java.lang.String)
	 */
	public void setTitle(String name) {
		super.setName(name);
	}

	/**
	 *  Inherited by IGefaehrdungsBaumElement.
	 *  A RisikoMassnahmenUmsetzung has no children.
	 *  
	 *  returns null
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		return null;
	}

	/**
	 * returns the parent element in the tree, which is a
	 * GefaehrdungsUmsetzung.
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		// TODO Auto-generated method stub
		return parent;
	}
	
	/**
	 * sets the parent element in the tree, which is a
	 * GefaehrdungsUmsetzung.
	 */
	public void setGefaehrdungsBaumParent(GefaehrdungsUmsetzung newParent) {
		// TODO Auto-generated method stub
		parent = newParent;
	}
	
	/**
	 *  returns the image to display in viewer.
	 */
	public Image getImage() {
		return image;
	}
	
	/**
	 *   returns the Name of the RisikoMassnahmenUmsetzung
	 */
	public String getText() {
		return this.getTitle();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.IGefaehrdungsBaumElement#getDescription()
	 */
	public String getDescription() {
		// TODO implement Description
		return "";
	}
}
