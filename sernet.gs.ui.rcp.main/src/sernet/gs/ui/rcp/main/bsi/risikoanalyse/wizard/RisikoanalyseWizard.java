package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
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
 * Processing of Gefährdungen.
 * 
 * @author ahanekop@sernet.de
 *
 */
public class RisikoanalyseWizard extends Wizard implements IExportWizard {

	private ChooseGefaehrdungPage chooseGefaehrdungPage;
	private EstimateGefaehrdungPage estimateGefaehrdungPage;
	private RiskHandlingPage riskHandlingPage;
	private AdditionalSecurityMeasuresPage additionalSecurityMeasuresPage;
	private IStructuredSelection selection;
	private IWorkbench workbench;
	private CnATreeElement cnaElement;
	private ArrayList<Gefaehrdung> allGefaehrdungen = new ArrayList<Gefaehrdung>();
	private ArrayList<Gefaehrdung> associatedGefaehrdungen = new ArrayList<Gefaehrdung>();
	private ArrayList<OwnGefaehrdung> ownGefaehrdungen = new ArrayList<OwnGefaehrdung>();
	private ArrayList<Gefaehrdung> notOKGefaehrdungen = new ArrayList<Gefaehrdung>();
	private ArrayList<GefaehrdungsUmsetzung> gefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();
	
	// Liste der als "A" eingestuften Risiken - RiskHandlingPage
	private ArrayList<GefaehrdungsUmsetzung> risikoGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();
	
	// Liste der MassnahmenUmsetzungen - AdditionalSecurityMeasuresPage
	private ArrayList<MassnahmenUmsetzung> massnahmenUmsetzungen = new ArrayList<MassnahmenUmsetzung>();
	
	/* hier Anwendungslogik einfügen */
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

	/* hier Anwendungslogik einfügen */
	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Entry point.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		loadAllGefaehrdungen();
		loadAssociatedGefaehrdungen();
		loadOwnGefaehrdungen();
		addOwnGefaehrdungen();
		// loadGefaehrdungsUmsetzungen();
	}
	
	
	public void addPages() {
		setWindowTitle("Risikoanalyse");
		
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
	 * preserve current List of Gefaehrdungen for the selected Baustein
	 * to make the Risikoanalyse for
	 * 
	 * @param associatedGefaehrdungen
	 */
	public void setAssociatedGefaehrdungen(
			ArrayList<Gefaehrdung> newAssociatedGefaehrdungen) {
		associatedGefaehrdungen = newAssociatedGefaehrdungen;
	}

	/**
	 * retrieve current List of Gefaehrdungen for the selected Baustein
	 * to make the Risikoanalyse for
	 * 
	 * @return zugeordneteGefaehrdungen  List of currently selected Gefaehrdungen
	 */
	public ArrayList<Gefaehrdung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}

	/**
	 * Selects All Gefaehrdungen
	 */
	private void loadAllGefaehrdungen() {
		List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance().getBausteine();
        alleBausteine: for (Baustein baustein : bausteine) {
        	alleGefaehrdungen: for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
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
	 * Selects All GefaehrdungsUmsetzungen
	 */
	private void loadGefaehrdungsUmsetzungen() {
		for (Gefaehrdung element : associatedGefaehrdungen) {
			GefaehrdungsUmsetzung newGefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(
					element);
			gefaehrdungsUmsetzungen.add(newGefaehrdungsUmsetzung);
		}
	}
	
	/**
	 * Get all Gefaehrdungen for a given Baustein by it's ID.
	 * 
	 * @param children  ??
	 * @return ArrayList of Gefaehrdungen 
	 */
	private void loadAssociatedGefaehrdungen() {
		Set<CnATreeElement> children = cnaElement.getChildren();
		for (CnATreeElement cnATreeElement : children) {
			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
			Baustein baustein = BSIKatalogInvisibleRoot.getInstance()
					.getBaustein(bausteinUmsetzung.getKapitel());
			if (baustein == null)
				continue;

			for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
				Boolean duplicate = false;
				alleTitel: for (Gefaehrdung element : associatedGefaehrdungen) {
					if (element.getTitel().equals(gefaehrdung.getTitel())) {
						duplicate = true;
						Logger.getLogger(this.getClass()).debug(
								"duplikat erkannt: " + gefaehrdung.getTitel());
						break alleTitel;
					}
				}
				if (!duplicate) {
					associatedGefaehrdungen.add(gefaehrdung);
				}
			}
		}
	}
	
	private void loadOwnGefaehrdungen() {
		ownGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
	}
	
	public void setAllGefaehrdungen(ArrayList<Gefaehrdung> newOwnGefaehrdungen) {
		allGefaehrdungen = newOwnGefaehrdungen;
	}
	
	public ArrayList<Gefaehrdung> getAllGefaehrdungen() {
		return allGefaehrdungen;
	}
	
	public void setOwnGefaehrdungen(ArrayList<OwnGefaehrdung> newOwnGefaehrdungen) {
		ownGefaehrdungen = newOwnGefaehrdungen;
	}
	
	public ArrayList<OwnGefaehrdung> getOwnGefaehrdungen() {
		return ownGefaehrdungen;
	}
	
	public void setGefaehrdungsUmsetzungen(ArrayList<GefaehrdungsUmsetzung> newGefaehrdungsUmsetzungen) {
		gefaehrdungsUmsetzungen = newGefaehrdungsUmsetzungen;
	}
	
	public ArrayList<GefaehrdungsUmsetzung> getGefaehrdungsUmsetzungen() {
		return gefaehrdungsUmsetzungen;
	}
	
	public void setNotOKGefaehrdungen(ArrayList<Gefaehrdung> newNotOKGefaehrdungen) {
		notOKGefaehrdungen = newNotOKGefaehrdungen;
	}
	
	public ArrayList<Gefaehrdung> getNotOKGefaehrdungen() {
		return notOKGefaehrdungen;
	}
	
	public void setSelectionElement(CnATreeElement treeElement) {
		cnaElement = treeElement;
	}
	
	// TODO habe ich von private auf public gesetzt - ist das ein Problem?
	public CnATreeElement getSelectionElement() {
		return cnaElement;
	}
	
	public void addOwnGefaehrdungen() {
		for (OwnGefaehrdung element : ownGefaehrdungen) {
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
	 * @return the risikoGefaehrdungsUmsetzungen (list of GefaehrdungsUmsetzungen)
	 */
	public ArrayList<GefaehrdungsUmsetzung> getRisikoGefaehrdungsUmsetzungen() {
		return risikoGefaehrdungsUmsetzungen;
	}

	/**
	 * @param risikoGefaehrdungsUmsetzungen the risikoGefaehrdungsUmsetzungen to set
	 */
	public void setRisikoGefaehrdungsUmsetzungen(
			ArrayList<GefaehrdungsUmsetzung> newRisikoGefaehrdungsUmsetzungen) {
		risikoGefaehrdungsUmsetzungen = newRisikoGefaehrdungsUmsetzungen;
	}

	/**
	 * Adds all GefaehrdungsUmsetzungen with Alternative "A" to the ArrayList
	 * gefaehrdungsUmsetzungen. Only run once, to initialize the List.
	 */
	public void addRisikoGefaehrdungsUmsetzungen() {
		for (GefaehrdungsUmsetzung element : gefaehrdungsUmsetzungen) {
			/* add to List of "A" categorized risks if needed */
			if (element.getAlternative() == GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_A
					&& !(risikoGefaehrdungsUmsetzungen.contains(element))) {
				risikoGefaehrdungsUmsetzungen.add(element);
				Logger.getLogger(this.getClass()).debug("Add Risiko: " + element.getTitel());
			}
		}
	}

	/**
	 * @return the massnahmenUmsetzungen ArrayList
	 */
	public ArrayList<MassnahmenUmsetzung> getMassnahmenUmsetzungen() {
		return massnahmenUmsetzungen;
	}

	/**
	 * @param massnahmenUmsetzungen the massnahmenUmsetzungen to set
	 */
	public void setMassnahmenUmsetzungen(
			ArrayList<MassnahmenUmsetzung> newMassnahmenUmsetzungen) {
		massnahmenUmsetzungen = newMassnahmenUmsetzungen;
	}
	
}
