/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

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
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.wizards.ChooseExportMethodPage;
import sernet.gs.ui.rcp.main.bsi.wizards.ChoosePropertiesPage;
import sernet.gs.ui.rcp.main.bsi.wizards.ChooseReportPage;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.PropertiesRow;
import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.main.reports.TextReport;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
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
	private RisikoMassnahme massnahme;
	
	protected RisikoMassnahmenUmsetzung(CnATreeElement superParent, 
			GefaehrdungsUmsetzung myParent, RisikoMassnahme massnahme) {
		super(superParent);
		this.parent = myParent;
		this.massnahme = massnahme;
		setStufe('Z');
	}
	
	protected RisikoMassnahmenUmsetzung(CnATreeElement superParent, 
			GefaehrdungsUmsetzung myParent) {
		super(superParent);
		this.parent = myParent;
	}
	
	private RisikoMassnahmenUmsetzung() {
		// hibernate constructor
	}

	/**
	 * Must be implemented due to Interface IGefaehrdungsBaumElement.
	 * 
	 * A RisikoMassnahmenUmsetzung never has children, therefore
	 * always returns null.
	 *  
	 * @return - null
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		return null;
	}

	/**
	 * Must be implemented due to Interface IGefaehrdungsBaumElement.
	 * 
	 * Returns the parent element in the tree, which is a
	 * GefaehrdungsUmsetzung.
	 * 
	 * @return - the parent element "parent" (IGefaehrdungsBaumElement)
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		// TODO Auto-generated method stub
		return parent;
	}
	
	/**
	 * Sets the parent element "parent" (GefaehrdungsUmsetzung) in the tree
	 * if the parent is null, else nothing.
	 * 
	 * @param newParent - new GefaehrdungsUmsetzung which is to be
	 * 		  the new parent
	 */
	public void setGefaehrdungsBaumParent(GefaehrdungsUmsetzung newParent) {
		if (parent == null ) { 
			parent = newParent;
		}
	}
	
	/**
	 * Must be implemented due to Interface IGefaehrdungsBaumElement.
	 * 
	 * Calls the local getTitle() method.
	 * 
	 * @return - name of the RisikoMassnahmenUmsetzung
	 */
	public String getText() {
		return this.getTitel();
	}
	
	/**
	 * Overrides and calls MassnahmenUmsetzung.getTitle().
	 * 
	 * @return - title of the RisikoMassnahmenUmsetzung
	 */
	@Override
	public String getTitel() {
		return super.getName();
	}
	
	/**
	 * Implemented for reasons of conformity.
	 * 
	 * Calls MassnahmenUmsetzung.setName() to set the title of a
	 * RisikoMassnahmenUmsetzung.
	 * 
	 * @param name - new name of the RisikoMassnahmenUmsetzung
	 */
	public void setTitle(String name) {
		super.setName(name);
	}

	/**
	 * Must be implemented due to Interface IGefaehrdungsBaumElement.
	 * 
	 * @return - description (String) of the RisikoMassnahmenUmsetzung.
	 */
	public String getDescription() {
		return getRisikoMassnahme().getDescription();
	}
	
	/**
	 * Returns the nuber of the Massnahme.
	 * 
	 * @return the number
	 */
	public String getNumber() {
		return getKapitel();
	}

	/**
	 * Sets the number of the Massnahme.
	 * 
	 * @param newNumber the number to set
	 */
	public void setNumber(String newNumber) {
		setKapitel(newNumber);
	}
	

	/**
	 * Returns an instance of the RisikoMassnahme.
	 *  
	 * @return an instance of the RisikoMassnahme
	 */
	public RisikoMassnahme getRisikoMassnahme() {
		if (massnahme == null) {
			try {
				massnahme = RisikoMassnahmeHome.getInstance().loadByNumber(getNumber());
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Datenzugriff");
				return null;
			}
		}

		return massnahme;
	}
}
