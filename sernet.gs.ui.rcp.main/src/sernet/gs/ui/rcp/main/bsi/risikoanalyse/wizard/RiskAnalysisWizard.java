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
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisListsHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
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
 * Wizard to accomplish a 'BSI-Standard 100-3' risk-analysis.
 * 
 * @author ahanekop@sernet.de
 */
public class RiskAnalysisWizard extends Wizard implements IExportWizard {

	private AdditionalSecurityMeasuresPage additionalSecurityMeasuresPage;
	private boolean canFinish = false;
	private ChooseGefaehrdungPage chooseGefaehrdungPage;
	private CnATreeElement cnaElement;
	private EstimateGefaehrdungPage estimateGefaehrdungPage;
	private RiskHandlingPage riskHandlingPage;

	/*
	 * Element to save all relevant data in DB on completion of wizard. Also
	 * parent for all GefährdungsUmsetzung objects.
	 */
	private FinishedRiskAnalysis finishedRiskAnalysis = null;

	/* list of all Gefaehrdungen - used in ChooseGefaehrungPage */
	private ArrayList<Gefaehrdung> allGefaehrdungen = new ArrayList<Gefaehrdung>();

	/*
	 * list of all own Gefaehrdungen of type OwnGefaehrdung - used in
	 * ChooseGefaehrungPage
	 */
	private ArrayList<OwnGefaehrdung> allOwnGefaehrdungen = new ArrayList<OwnGefaehrdung>();

	/* list of all MassnahmenUmsetzungen - AdditionalSecurityMeasuresPage */
	private ArrayList<MassnahmenUmsetzung> allMassnahmenUmsetzungen = new ArrayList<MassnahmenUmsetzung>();

	/* Are we editing a previous Risk Analysis? */
	private boolean previousAnalysis = false;
	private FinishedRiskAnalysisLists finishedRiskAnalysisLists;

	/**
	 * Constructor of wizard. Sets the needed data.
	 * 
	 * @param newCnaElement a base class element of type CnATreeElement
	 * 			to make the risk analysis for
	 */
	public RiskAnalysisWizard(CnATreeElement newCnaElement) {
		cnaElement = newCnaElement;
	}

	/**
	 * Constructor of wizard. Sets the needed data.
	 * 
	 * @param newCnaElement a base class element of type CnATreeElement
	 * 			to make the risk analysis for
	 * @param analysis a base class element of type FinishedRiskAnalysis
	 */
	public RiskAnalysisWizard(CnATreeElement newCnaElement,
			FinishedRiskAnalysis analysis) {
		this(newCnaElement);
		finishedRiskAnalysis = analysis;
	}

	/**
	 * Save the first RiskAnalysis for a base class element or
	 * update an already existing.
	 * 
	 * @return true if save/update was successful, false else
	 */
	@Override
	public boolean performFinish() {
		
		cnaElement.addChild(finishedRiskAnalysis);
		
		if (!previousAnalysis) {
			try {
				CnAElementHome.getInstance().save(finishedRiskAnalysis);
				finishedRiskAnalysisLists.setFinishedRiskAnalysisId(finishedRiskAnalysis
						.getDbId());
				FinishedRiskAnalysisListsHome.getInstance().saveNew(finishedRiskAnalysisLists);
			} catch (Exception e) {
				ExceptionUtil.log(e,
						"Konnte neue Risikoanalyse nicht speichern.");
			}
			
		} else {
			try {
				CnAElementHome.getInstance().update(finishedRiskAnalysis);
				FinishedRiskAnalysisListsHome.getInstance().update(finishedRiskAnalysisLists);
			} catch (Exception e) {
				ExceptionUtil
						.log(e,
								"Konnte Änderungen an vorheriger Risikoanalyse nicht speichern.");
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
	 * Entry point.
	 * Initialize needed objects.
	 * 
	 * @param workbench the current workbench
	 * @param selection the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
		if (finishedRiskAnalysis == null) {
			finishedRiskAnalysis = new FinishedRiskAnalysis(cnaElement);
			finishedRiskAnalysisLists = new FinishedRiskAnalysisLists();
			
		} else {
			finishedRiskAnalysisLists = FinishedRiskAnalysisListsHome.getInstance().loadById(
					finishedRiskAnalysis.getDbId());
			previousAnalysis = true;
		}

		loadAllGefaehrdungen();
		loadAllMassnahmen();
		loadAssociatedGefaehrdungen();
		loadOwnGefaehrdungen();
		addOwnGefaehrdungen();

		addRisikoMassnahmenUmsetzungen(loadRisikoMassnahmen());

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
	 * Saves all Gefaehrdungen availiable from BSI IT-Grundschutz-Kataloge
	 * in a list.
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

		NullModel nullModel = new NullModel() {
			@Override
			public boolean canContain(Object obj) {
				return true;
			}
		};

		allBausteine: for (Baustein baustein : bausteine) {
			allMassnahmen: for (Massnahme massnahme : baustein.getMassnahmen()) {
				Boolean duplicate = false;
				allTitel: for (MassnahmenUmsetzung vorhandeneMassnahmenumsetzung : allMassnahmenUmsetzungen) {
					if (vorhandeneMassnahmenumsetzung.getName().equals(
							massnahme.getTitel())) {
						duplicate = true;
						break allTitel;
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
								.error("Fehler beim Erstellen der" +
										" Massnahmenumsetzung: ", e);
					} /* end try-catch */
				} /* end if */
			} /* end for allMassnahmen */
		} /* end for allBausteine */
	} /* end loadAllMassnahmen() */

	/**
	 * Saves all Gefaehrdungen associated to the chosen IT-system in a List.
	 */
	private void loadAssociatedGefaehrdungen() {
		
		Set<CnATreeElement> children = cnaElement.getChildren();
		
		CollectBausteine: for (CnATreeElement cnATreeElement : children) {
			if (!(cnATreeElement instanceof BausteinUmsetzung)) {
				continue CollectBausteine;
			}

			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
			Baustein baustein = BSIKatalogInvisibleRoot.getInstance()
					.getBaustein(bausteinUmsetzung.getKapitel());
			
			if (baustein == null) {
				continue;
			}

			for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
				Boolean duplicate = false;
				alleTitel: for (Gefaehrdung element : finishedRiskAnalysisLists
						.getAssociatedGefaehrdungen()) {
					if (element.getTitel().equals(gefaehrdung.getTitel())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					finishedRiskAnalysisLists.getAssociatedGefaehrdungen().add(gefaehrdung);
				} /* end if */
			} /* end for */
		} /* end for CollectBausteine */
	} /* end loadAssociatedGefaehrdungen() */

	/**
	 * Saves all own Gefaehrdungen in a List.
	 */
	private void loadOwnGefaehrdungen() {
		allOwnGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
	}
	
	/**
	 * Returns a list of all RisikoMassnahmen.
	 * 
	 * @return a list of all RisikoMassnahmen
	 */
	private ArrayList<RisikoMassnahme> loadRisikoMassnahmen() {
		return RisikoMassnahmeHome.getInstance().loadAll();
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
	 * Returns the List of all Gefaehrdungen of type GefaehrdungsUmsetzung.
	 * 
	 * @return ArrayList of GefaehrdungsUmsetzungen
	 */
	public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return finishedRiskAnalysisLists.getAllGefaehrdungsUmsetzungen();
	}

	/**
	 * Allows repeating the Wizard with result from a previous analysis.
	 * 
	 * @param newFinishedRiskAnalysis old analysis to repeat all steps
	 */
	public void setOldAnalysis(FinishedRiskAnalysis newFinishedRiskAnalysis) {
		finishedRiskAnalysis = newFinishedRiskAnalysis;
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
			if (!(finishedRiskAnalysisLists.getAssociatedGefaehrdungen().contains(element))) {
				finishedRiskAnalysisLists.getAssociatedGefaehrdungen().add(element);
			}
			/* add to list of all Gefaehrdungen */
			if (!(allGefaehrdungen.contains(element))) {
				allGefaehrdungen.add(element);
			}
		}
	}

	/**
	 * Adds the elements of given List to the List of all Massnahmen from BSI
	 * IT-Grundschutz-Kataloge via addRisikoMassnahmeUmsetzung().
	 * 
	 * @param allRisikoMassnahmen the List of elements to add 
	 */
	public void addRisikoMassnahmenUmsetzungen(
			List<RisikoMassnahme> allRisikoMassnahmen) {
		for (RisikoMassnahme massnahme : allRisikoMassnahmen) {
			addRisikoMassnahmeUmsetzung(massnahme);
		}
	}

	/**
	 * Creates an instance of RisikoMassnahmenUmsetzung from the given
	 * RisikoMassnahme and adds it to the List of all Massnahmen.
	 * 
	 * @param allRisikoMassnahmen the List of elements to add 
	 */
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
		return finishedRiskAnalysisLists.getNotOKGefaehrdungsUmsetzungen();
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
	 * @param newAllMassnahmenUmsetzungen a List of all Massnahmen of type
	 * 			MassnahmenUmsetzung
	 */
	public void setAllMassnahmenUmsetzungen(
			ArrayList<MassnahmenUmsetzung> newAllMassnahmenUmsetzungen) {
		allMassnahmenUmsetzungen = newAllMassnahmenUmsetzungen;
	}

	/**
	 * Returns whether this wizard could be finished without further user
	 * interaction.
	 * 
	 * @return true if wizard can be finished, false else
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
	 * Returns a base class object of an already finished risk analysis.
	 * 
	 * @return an object of an already finished risk analysis
	 */
	public CnATreeElement getFinishedRiskAnalysis() {
		return finishedRiskAnalysis;
	}
	
	/**
	 * Sets the List of Gefaehrdungen associated to the chosen IT-system.
	 * 
	 * @param newAssociatedGefaehrdungen ArrayList of Gefaehrdungen
	 */
	public void setAssociatedGefaehrdungen(
			ArrayList<Gefaehrdung> newAssociatedGefaehrdungen) {
		finishedRiskAnalysisLists.setAssociatedGefaehrdungen(newAssociatedGefaehrdungen);
	}
	
	/**
	 * Returns the current List of Gefaehrdungen associated to the chosen
	 * IT-System.
	 * 
	 * @return ArrayList of currently associated Gefaehrdungen
	 */
	public List<Gefaehrdung> getAssociatedGefaehrdungen() {
		return finishedRiskAnalysisLists.getAssociatedGefaehrdungen();
	}
}
