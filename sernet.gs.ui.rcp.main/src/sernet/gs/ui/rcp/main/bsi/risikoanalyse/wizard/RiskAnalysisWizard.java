package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Wizard to accomplish a 'BSI-Standard 100-3' risk-analysis. RiskAnalysisWizard
 * 
 * @author ahanekop@sernet.de
 */
public class RiskAnalysisWizard extends Wizard implements IExportWizard {

	private boolean canFinish = false;
	private CnATreeElement cnaElement;
	private ChooseGefaehrdungPage chooseGefaehrdungPage;
	private EstimateGefaehrdungPage estimateGefaehrdungPage;
	private RiskHandlingPage riskHandlingPage;
	private AdditionalSecurityMeasuresPage additionalSecurityMeasuresPage;

	/* list of all Gefaehrdungen - ChooseGefaehrungPage_OK */
	private ArrayList<Gefaehrdung> allGefaehrdungen =
			new ArrayList<Gefaehrdung>();

	/* list of all own Gefaehrdungen of type OwnGefaehrdung -
	 * ChooseGefaehrungPage_OK */
	private ArrayList<OwnGefaehrdung> allOwnGefaehrdungen =
			new ArrayList<OwnGefaehrdung>();

	/* list of all Gefaehrdungen of type GefaehrdungsUmsetzung -
	 * ChooseGefaehrdungPage_OK, EstimateGefaehrungPage_OK, RiskHandlingPage */
	private ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen =
			new ArrayList<GefaehrdungsUmsetzung>();

	/* list of all Massnahmen - AdditionalSecurityMeasuresPage */
	private ArrayList<Massnahme> allMassnahmen = new ArrayList<Massnahme>();

	/* list of all MassnahmenUmsetzungen - AdditionalSecurityMeasuresPage */
	private ArrayList<MassnahmenUmsetzung> allMassnahmenUmsetzungen =
			new ArrayList<MassnahmenUmsetzung>();

	/* list of all RisikoMassnahmenUmsetzungen - AdditionalSecurityMeasuresPage*/
	private ArrayList<RisikoMassnahmenUmsetzung> allRisikoMassnahmenUmsetzungen =
			new ArrayList<RisikoMassnahmenUmsetzung>();

	/* list of Gefaehrdungen associated to the chosen IT-system -
	 * ChooseGefaehrungPage_OK, EstimateGefaehrungPage_OK */
	private ArrayList<Gefaehrdung> associatedGefaehrdungen =
			new ArrayList<Gefaehrdung>();

	/* list of Gefaehrdungen, which need further processing -
	 * EstimateGefaehrungPage_OK */
	private ArrayList<Gefaehrdung> notOKGefaehrdungen =
			new ArrayList<Gefaehrdung>();

	/* list of Gefaehrdungen, which need additional security measures. - RiskHandlingPage */
	private ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen =
			new ArrayList<GefaehrdungsUmsetzung>();

	/**
	 * No special finish processing necessary.
	 * 
	 * @return always true
	 */
	@Override
	public boolean performFinish() {
		return true;
	}

	/**
	 * No special cancel processing necessary.
	 * 
	 * @return always true
	 */
	@Override
	public boolean performCancel() {
		return true;
	}

	/**
	 * Needs to be implemented because of IExportWizard/IWorkbenchWizard.
	 * Entry point.
	 * 
	 * @param workbench the current workbench
	 * @param selection the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		loadAllGefaehrdungen();
		loadAllMassnahmen();
		loadAssociatedGefaehrdungen();
		loadOwnGefaehrdungen();
		addOwnGefaehrdungen();
	}

	/**
	 * Adds extra pages to the Wizard.
	 */
	public void addPages() {
		setWindowTitle("Risikoanalyse auf Basis von IT-Grundschutz");

		chooseGefaehrdungPage = new ChooseGefaehrdungPage();
		addPage(chooseGefaehrdungPage);

		estimateGefaehrdungPage = new EstimateGefaehrdungPage();
		addPage(estimateGefaehrdungPage);

		riskHandlingPage = new RiskHandlingPage();
		addPage(riskHandlingPage);

		additionalSecurityMeasuresPage = new AdditionalSecurityMeasuresPage();
		addPage(additionalSecurityMeasuresPage);
	}

	/**
	 * Sets the List of Gefaehrdungen associated to the chosen IT-system. 
	 * 
	 * @param newAssociatedGefaehrdungen ArrayList of Gefaehrdungen
	 */
	public void setAssociatedGefaehrdungen(
			ArrayList<Gefaehrdung> newAssociatedGefaehrdungen) {
		associatedGefaehrdungen = newAssociatedGefaehrdungen;
	}

	/**
	 * Returns the current List of Gefaehrdungen associated to the chosen
	 * IT-system.
	 * 
	 * @return ArrayList of currently associated Gefaehrdungen
	 */
	public ArrayList<Gefaehrdung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}

	/**
	 * Saves all Gefaehrdungen availiable from BSI IT-Grundschutz-Kataloge.
	 */
	private void loadAllGefaehrdungen() {
		List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance()
				.getBausteine();
		alleBausteine: for (Baustein baustein : bausteine) {
			alleGefaehrdungen: for (Gefaehrdung gefaehrdung : baustein
					.getGefaehrdungen()) {
				Boolean duplicate = false;
				alleTitel: for (Gefaehrdung element : allGefaehrdungen) {
					if (element.getTitel().equals(gefaehrdung.getTitel())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					allGefaehrdungen.add(gefaehrdung);
				}
			}
		}
	}

	/**
	 * Saves all Massnahmen for the chosen IT-system in a List.
	 */
	private void loadAllMassnahmen() {
		List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance()
				.getBausteine();
		alleBausteine: for (Baustein baustein : bausteine) {
			alleMassnahmen: for (Massnahme massnahme : baustein.getMassnahmen()) {
				Boolean duplicate = false;
				alleTitel: for (Massnahme element : allMassnahmen) {
					if (element.getTitel().equals(massnahme.getTitel())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					MassnahmenUmsetzung massnahmeUmsetzung =
							new MassnahmenUmsetzung(cnaElement);
					massnahmeUmsetzung.setName(massnahme.getTitel());
					allMassnahmen.add(massnahme);
					allMassnahmenUmsetzungen.add(massnahmeUmsetzung);
				}
			}
		}
	}

	/**
	 * Saves all Gefaehrdungen associated to the chosen IT-system in a List.
	 */
	private void loadAssociatedGefaehrdungen() {
		Set<CnATreeElement> children = cnaElement.getChildren();
		for (CnATreeElement cnATreeElement : children) {
			BausteinUmsetzung bausteinUmsetzung =
					(BausteinUmsetzung) cnATreeElement;
			Baustein baustein = BSIKatalogInvisibleRoot.getInstance()
					.getBaustein(bausteinUmsetzung.getKapitel());
			if (baustein == null)
				continue;

			for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
				Boolean duplicate = false;
				alleTitel: for (Gefaehrdung element : associatedGefaehrdungen) {
					if (element.getTitel().equals(gefaehrdung.getTitel())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					associatedGefaehrdungen.add(gefaehrdung);
				}
			}
		}
	}

	/**
	 * Saves all own Gefaehrdungen in a List.
	 */
	private void loadOwnGefaehrdungen() {
		allOwnGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
	}

	/**
	 * Sets the List of all Gefaehrdungen.
	 * 
	 * @param newAllGefaehrdungen ArrayList of all Gefaehrdungen
	 */
	public void setAllGefaehrdungen(ArrayList<Gefaehrdung> newAllGefaehrdungen) {
		allGefaehrdungen = newAllGefaehrdungen;
	}

	/**
	 * Returns the List of all Gefaehrdungen availiable from
	 * BSI IT-Grundschutz-Kataloge plus the own Gefaehrdungen.
	 * 
	 * @return ArrayList of all Gefaehrdungen
	 */
	public ArrayList<Gefaehrdung> getAllGefaehrdungen() {
		return allGefaehrdungen;
	}

	/**
	 * Sets the List of all own Gefaehrdungen.
	 * 
	 * @param newAllOwnGefaehrdungen List of own Gefaehrdungen
	 */
	public void setAllOwnGefaehrdungen(
			ArrayList<OwnGefaehrdung> newAllOwnGefaehrdungen) {
		allOwnGefaehrdungen = newAllOwnGefaehrdungen;
	}

	/**
	 * Returns the List of all own Gefaehrdungen.
	 * 
	 * @return ArrayList of all own Gefaehrdungen
	 */
	public ArrayList<OwnGefaehrdung> getAllOwnGefaehrdungen() {
		return allOwnGefaehrdungen;
	}

	/**
	 * Saves the List of all Gefaehrdungen of type GefaehrdungsUmsetzung.
	 * 
	 * @param newAllGefaehrdungsUmsetzungen
	 */
	public void setAllGefaehrdungsUmsetzungen(
			ArrayList<GefaehrdungsUmsetzung> newAllGefaehrdungsUmsetzungen) {
		allGefaehrdungsUmsetzungen = newAllGefaehrdungsUmsetzungen;
	}

	/**
	 * Returns the List of all Gefaehrdungen of type GefaehrdungsUmsetzung.
	 * 
	 * @return ArrayList of GefaehrdungsUmsetzung
	 */
	public ArrayList<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return allGefaehrdungsUmsetzungen;
	}

	/**
	 * Sets the List of all Gefaehrdungen which need further processing.
	 * 
	 * @param newNotOKGefaehrdungen ArrayList of Gefaehrdungen
	 */
	public void setNotOKGefaehrdungen(
			ArrayList<Gefaehrdung> newNotOKGefaehrdungen) {
		notOKGefaehrdungen = newNotOKGefaehrdungen;
	}

	/**
	 * Returns the List of all Gefaehrdungen which need further processing.
	 * 
	 * @return ArrayList of Gefaehrdungen which need further processing
	 */
	public ArrayList<Gefaehrdung> getNotOKGefaehrdungen() {
		return notOKGefaehrdungen;
	}

	/**
	 * Sets the chosen IT-system to make the risk-analysis for.
	 * 
	 * @param treeElement the IT-system to make the risk-analysis for
	 */
	public void setCnaElement(CnATreeElement treeElement) {
		cnaElement = treeElement;
	}

	/**
	 * Returns the chosen IT-system to make the risk-analysis for.
	 * 
	 * @return the IT-system to make the risk-analysis for
	 */
	public CnATreeElement getCnaElement() {
		return cnaElement;
	}

	/**
	 * Adds the own Gefaehrdungen to the List of all Gefaehrdungen from BSI
	 * IT-Grundschutz-Kataloge.
	 */
	public void addOwnGefaehrdungen() {
		for (OwnGefaehrdung element : allOwnGefaehrdungen) {
			/* add to List of selected Gefaehrdungen */
			if (!(associatedGefaehrdungen.contains(element))) {
				associatedGefaehrdungen.add(element);
			}
			/* add to list of all Gefaehrdungen */
			if (!(allGefaehrdungen.contains(element))) {
				allGefaehrdungen.add(element);
			}
		}
	}

	/**
	 * Adds the own Massnahmen to the List of all Massnahmen from BSI
	 * IT-Grundschutz-Kataloge.
	 */
	public void addRisikoMassnahmenUmsetzungen() {
		for (RisikoMassnahmenUmsetzung element : allRisikoMassnahmenUmsetzungen) {
			/* add to list of all MassnahmenUmsetzungen */
			if (!(allMassnahmenUmsetzungen.contains(element))) {
				allMassnahmenUmsetzungen.add(element);
			}
		}
	}

	/**
	 * Returns the List of Gefaehrdungen which need additional security measures.
	 * 
	 * @return the List of Gefaehrdungen of type GefaehrdungsUmsetzung
	 */
	public ArrayList<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
		return notOKGefaehrdungsUmsetzungen;
	}

	/**
	 * Sets the List of Gefaehrdungen which need additional security measures.
	 * 
	 * @param notOKGefaehrdungsUmsetzungen List of Gefaehrdungen of type
	 * 		  GefaehrdungsUmsetzung, which need additional security measures
	 */
	public void setNotOKGefaehrdungsUmsetzungen(
			ArrayList<GefaehrdungsUmsetzung> newNotOKGefaehrdungsUmsetzungen) {
		notOKGefaehrdungsUmsetzungen = newNotOKGefaehrdungsUmsetzungen;
	}

	/**
	 * Adds all GefaehrdungsUmsetzungen with Alternative "A" to the List of
	 * Gefaehrdungen which need additional security measures.
	 */
	public void addRisikoGefaehrdungsUmsetzungen() {
		for (GefaehrdungsUmsetzung element : allGefaehrdungsUmsetzungen) {
			/* add to List of "A" categorized risks if needed */
			if (element.getAlternative() == 
					GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_A
					&& !(notOKGefaehrdungsUmsetzungen.contains(element))) {
				notOKGefaehrdungsUmsetzungen.add(element);
				Logger.getLogger(this.getClass()).debug(
						"Add Risiko: " + element.getTitel());
			}
		}
	}

	/**
	 * Returns the List of all Massnahmen of type MassnahmenUmsetzung.
	 * 
	 * @return ArrayList of all Massnahmen of type MassnahmenUmsetzung
	 */
	public ArrayList<MassnahmenUmsetzung> getAllMassnahmenUmsetzungen() {
		return allMassnahmenUmsetzungen;
	}

	/**
	 * Saves the List of all Massnahmen of type MassnahmenUmsetzung.
	 * 
	 * @param newAllMassnahmenUmsetzungen the allMassnahmenUmsetzungen to set
	 */
	public void setAllMassnahmenUmsetzungen(
			ArrayList<MassnahmenUmsetzung> newAllMassnahmenUmsetzungen) {
		allMassnahmenUmsetzungen = newAllMassnahmenUmsetzungen;
	}

	/**
	 * Returns all Massnahmen of type RisikoMassnahmenUmsetzung.
	 * 
	 * @return ArrayList of all Massnahmen of type RisikoMassnahmenUmsetzung
	 */
	public ArrayList<RisikoMassnahmenUmsetzung> getAllRisikoMassnahmenUmsetzungen() {
		return allRisikoMassnahmenUmsetzungen;
	}

	/**
	 * Saves the List of all Massnahmen of type RisikoMassnahmenUmsetzung
	 * 
	 * @param newAllRisikoMassnahmenUmsetzungen ArrayList of all Massnahmen of
	 * 		  type RisikoMassnahmenUmsetzung
	 */
	public void setAllRisikoMassnahmenUmsetzungen(
			ArrayList<RisikoMassnahmenUmsetzung> newAllRisikoMassnahmenUmsetzungen) {
		allRisikoMassnahmenUmsetzungen = newAllRisikoMassnahmenUmsetzungen;
	}

	/**
	 * Returns whether this wizard could be finished without further user
	 * interaction.
	 * 
	 * @return true, if wizard can be finished, false else
	 */
	@Override
	public boolean canFinish() {
		return canFinish;
	}

	/**
	 * Saves a new state of canFinish.
	 * 
	 * @param newCanFinish set true, if wizard can be finished at this point,
	 * 		  false else
	 */
	public void setCanFinish(boolean newCanFinish) {
		canFinish = newCanFinish;
	}
}