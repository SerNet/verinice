package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisListsHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NullModel;

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

	/**
	 * Element to save all relevant data in DB on completion of wizard. Also
	 * parent for all GefährdungsUmsetzung objects.
	 */
	private FinishedRiskAnalysis finishedRiskAnalysis = null;

	/* list of all Gefaehrdungen - ChooseGefaehrungPage_OK */
	private ArrayList<Gefaehrdung> allGefaehrdungen = new ArrayList<Gefaehrdung>();

	/*
	 * list of all own Gefaehrdungen of type OwnGefaehrdung -
	 * ChooseGefaehrungPage_OK
	 */
	private ArrayList<OwnGefaehrdung> allOwnGefaehrdungen = new ArrayList<OwnGefaehrdung>();

	/* list of all MassnahmenUmsetzungen - AdditionalSecurityMeasuresPage */
	private ArrayList<MassnahmenUmsetzung> allMassnahmenUmsetzungen = new ArrayList<MassnahmenUmsetzung>();
	
	// Are we editing a previous Risk Analysis?
	private boolean previousAnalysis = false;
	private FinishedRiskAnalysisLists finishedRiskLists;
	
	private Collection<GefaehrdungsUmsetzung> objectsToDelete = new HashSet<GefaehrdungsUmsetzung>(50); 

	public RiskAnalysisWizard(CnATreeElement treeElement) {
		cnaElement = treeElement;
	}

	public RiskAnalysisWizard(CnATreeElement parent,
			FinishedRiskAnalysis analysis) {
		this(parent);
		finishedRiskAnalysis = analysis;
	}

	@Override
	public boolean performFinish() {
		cnaElement.addChild(finishedRiskAnalysis);
		if (!previousAnalysis) {
			try {
				CnAElementHome.getInstance().save(finishedRiskAnalysis);
				finishedRiskLists.setFinishedRiskAnalysisId(finishedRiskAnalysis.getDbId());
				FinishedRiskAnalysisListsHome.getInstance().saveNew(finishedRiskLists);
			} catch (Exception e) {
				ExceptionUtil.log(e, "Konnte neue Risikoanalyse nicht speichern.");
			}
		}
		else {
			try {
				for (GefaehrdungsUmsetzung umsetzung : objectsToDelete) {
					umsetzung.remove();
					CnAElementHome.getInstance().remove(umsetzung);
				}
				CnAElementHome.getInstance().update(finishedRiskAnalysis);
				FinishedRiskAnalysisListsHome.getInstance().update(finishedRiskLists);
			} catch (Exception e) {
				ExceptionUtil.log(e, "Konnte Änderungen an vorheriger Risikoanalyse nicht speichern.");
			}
		}
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
	 * Needs to be implemented because of IExportWizard/IWorkbenchWizard. Entry
	 * point.
	 * 
	 * @param workbench
	 *            the current workbench
	 * @param selection
	 *            the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (finishedRiskAnalysis == null) {
			finishedRiskAnalysis = new FinishedRiskAnalysis(cnaElement);
			finishedRiskLists = new FinishedRiskAnalysisLists();
		}
		else {
			finishedRiskLists = FinishedRiskAnalysisListsHome.getInstance().loadById(finishedRiskAnalysis.getDbId());
			if (finishedRiskLists == null) {
				ExceptionUtil.log(new Exception(), "Die Risikoanalyse wurde unvollständig gespeichert und " +
						"kann nicht wiederaufgenommen werden.");
				finishedRiskLists = new FinishedRiskAnalysisLists();
			}
			else 
				previousAnalysis = true;
		}
		
		loadAllGefaehrdungen();
		loadAllMassnahmen();
		loadAssociatedGefaehrdungen();

		loadOwnGefaehrdungen();
		addOwnGefaehrdungen();

		addRisikoMassnahmenUmsetzungen(loadRisikomassnahmen());

	}

	private ArrayList<RisikoMassnahme> loadRisikomassnahmen() {
		return RisikoMassnahmeHome.getInstance().loadAll();
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
	 * @param newAssociatedGefaehrdungen
	 *            ArrayList of Gefaehrdungen
	 */
	public void setAssociatedGefaehrdungen(
			ArrayList<GefaehrdungsUmsetzung> newAssociatedGefaehrdungen) {
		finishedRiskLists.setAssociatedGefaehrdungen( newAssociatedGefaehrdungen);
	}

	/**
	 * Returns the current List of Gefaehrdungen associated to the chosen
	 * IT-system.
	 * 
	 * @return ArrayList of currently associated Gefaehrdungen
	 */
	public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
		return finishedRiskLists.getAssociatedGefaehrdungen();
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
				alleTitel: for (IGSModel element : allGefaehrdungen) {
					if (element.getId().equals(gefaehrdung.getId())) {
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

		NullModel nullModel = new NullModel() {
			@Override
			public boolean canContain(Object obj) {
				return true;
			}
		};

		alleBausteine: for (Baustein baustein : bausteine) {
			alleMassnahmen: for (Massnahme massnahme : baustein.getMassnahmen()) {
				Boolean duplicate = false;
				alleTitel: for (MassnahmenUmsetzung vorhandeneMassnahmenumsetzung : allMassnahmenUmsetzungen) {
					if (vorhandeneMassnahmenumsetzung.getName().equals(
							massnahme.getTitel())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					MassnahmenUmsetzung massnahmeUmsetzung;
					try {
						massnahmeUmsetzung = (MassnahmenUmsetzung) CnAElementFactory
								.getInstance().saveNew(nullModel,
										MassnahmenUmsetzung.TYPE_ID,
										new BuildInput<Massnahme>(massnahme));
						allMassnahmenUmsetzungen.add(massnahmeUmsetzung);
					} catch (Exception e) {
						Logger
								.getLogger(this.getClass())
								.error(
										"Fehler beim Erstellen der Massnahmenumsetzung: ",
										e);
					}

				}
			}
		}
	}

	/**
	 * Saves all Gefaehrdungen associated to the chosen IT-system in a List.
	 */
	private void loadAssociatedGefaehrdungen() {
		Set<CnATreeElement> children = cnaElement.getChildren();
		CollectBausteine: for (CnATreeElement cnATreeElement : children) {
			if (!(cnATreeElement instanceof BausteinUmsetzung))
				continue CollectBausteine;

			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
			Baustein baustein = BSIKatalogInvisibleRoot.getInstance()
					.getBaustein(bausteinUmsetzung.getKapitel());
			if (baustein == null)
				continue;

			for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
				if (!GefaehrdungsUtil.listContainsById(finishedRiskLists.getAssociatedGefaehrdungen(), gefaehrdung)) {
					finishedRiskLists.getAssociatedGefaehrdungen().add(
							GefaehrdungsUmsetzungFactory.build(finishedRiskAnalysis, gefaehrdung));
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
	 * @param newAllGefaehrdungen
	 *            ArrayList of all Gefaehrdungen
	 */
	public void setAllGefaehrdungen(ArrayList<Gefaehrdung> newAllGefaehrdungen) {
		allGefaehrdungen = newAllGefaehrdungen;
	}

	/**
	 * Returns the List of all Gefaehrdungen availiable from BSI
	 * IT-Grundschutz-Kataloge plus the own Gefaehrdungen.
	 * 
	 * @return ArrayList of all Gefaehrdungen
	 */
	public ArrayList<Gefaehrdung> getAllGefaehrdungen() {
		return allGefaehrdungen;
	}

	/**
	 * Sets the List of all own Gefaehrdungen.
	 * 
	 * @param newAllOwnGefaehrdungen
	 *            List of own Gefaehrdungen
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
	 * Returns the List of all Gefaehrdungen of type GefaehrdungsUmsetzung.
	 * 
	 * @return ArrayList of GefaehrdungsUmsetzung
	 */
	public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return finishedRiskLists.getAllGefaehrdungsUmsetzungen();
	}

	/**
	 * Allows repeating the Wizard with result from a previous anaylsis.
	 * @param oldAnalysis old analysis to repeat all steps.
	 */
	public void setOldAnalysis(FinishedRiskAnalysis oldAnalysis) {
		this.finishedRiskAnalysis = oldAnalysis;
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
	public void addRisikoMassnahmenUmsetzungen(
			List<RisikoMassnahme> allRisikoMassnahmen) {
		for (RisikoMassnahme massnahme : allRisikoMassnahmen) {
			addRisikoMassnahmeUmsetzung(massnahme);

		}
	}

	public void addRisikoMassnahmeUmsetzung(RisikoMassnahme massnahme) {
		try {
			RisikoMassnahmenUmsetzung massnahmeUmsetzung = RisikoMassnahmenUmsetzungFactory
					.buildFromRisikomassnahme(massnahme, null, null);

			/* add to list of all MassnahmenUmsetzungen */
			if (!(allMassnahmenUmsetzungen.contains(massnahmeUmsetzung))) {
				allMassnahmenUmsetzungen.add(massnahmeUmsetzung);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					"Fehler beim Erstellen der Massnahmenumsetzung: ", e);
		}
	}

	/**
	 * Returns the List of Gefaehrdungen which need additional security
	 * measures.
	 * 
	 * @return the List of Gefaehrdungen of type GefaehrdungsUmsetzung
	 */
	public List<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
		return finishedRiskLists.getNotOKGefaehrdungsUmsetzungen();
	}

// not needed: all are already added by PropertiesComboBoxCellModifier.
//	/**
//	 * Adds all GefaehrdungsUmsetzungen with Alternative "A" to the List of
//	 * Gefaehrdungen which need additional security measures.
//	 */
//	public void addRisikoGefaehrdungsUmsetzungen() {
//		for (GefaehrdungsUmsetzung element : riskLists.getAllGefaehrdungsUmsetzungen()) {
//			/* add to List of "A" categorized risks if needed */
//			if (element.getAlternative() == GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_A
//					&& !(riskLists.getNotOKGefaehrdungsUmsetzungen().contains(element))) {
//				riskLists.getNotOKGefaehrdungsUmsetzungen().add(element);
//				Logger.getLogger(this.getClass()).debug(
//						"Add Risiko: " + element.getTitel());
//			}
//		}
//	}

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
	 * @param newAllMassnahmenUmsetzungen
	 *            the allMassnahmenUmsetzungen to set
	 */
	public void setAllMassnahmenUmsetzungen(
			ArrayList<MassnahmenUmsetzung> newAllMassnahmenUmsetzungen) {
		allMassnahmenUmsetzungen = newAllMassnahmenUmsetzungen;
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
	 * @param newCanFinish
	 *            set true, if wizard can be finished at this point, false else
	 */
	public void setCanFinish(boolean newCanFinish) {
		canFinish = newCanFinish;
	}

	/**
	 * Returns the parent Element of .
	 * 
	 * @return the parent Element
	 */
	public CnATreeElement getFinishedRiskAnalysis() {
		return this.finishedRiskAnalysis;
	}

	public Collection<GefaehrdungsUmsetzung> getObjectsToDelete() {
		return this.objectsToDelete;
	}

	public void removeOldObjects() {
		for (GefaehrdungsUmsetzung gefaehrdung : getObjectsToDelete()) {
			Logger.getLogger(this.getClass()).debug("Removing object from wizard: " + gefaehrdung);
			GefaehrdungsUtil.removeBySameId(getAllGefaehrdungsUmsetzungen(), gefaehrdung);
			GefaehrdungsUtil.removeBySameId(getNotOKGefaehrdungsUmsetzungen(), gefaehrdung);
		}
	}
}