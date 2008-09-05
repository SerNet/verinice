package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Top level category for all BSI model elements.
 * @author koderman@sernet.de
 *
 */
public class BSIModel extends CnATreeElement 
	implements IBSIStrukturElement {
	
	private static final String TYPE_ID = "bsimodel"; //$NON-NLS-1$
	
	public static final double DB_VERSION = 0.91D; 
	
	private Double dbVersion = 0D;
	
	private List<IBSIModelListener> listeners = new ArrayList<IBSIModelListener>();

	public BSIModel() {
		super(null);
		dbVersion = DB_VERSION;
	}
	
	public String getKuerzel() {
		return "";
	}
	
	public int getSchicht() {
		return 1;
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public String getTitel() {
		return Messages.BSIModel_1;
	}
	
	@Override
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		for (IBSIModelListener listener : listeners) {
			listener.childAdded(category, child);
		}
	}
	
	@Override
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		for (IBSIModelListener listener : listeners) {
			listener.childRemoved(category, child);
		}
	}
	
	@Override
	public void childChanged(CnATreeElement category, CnATreeElement child) {
		for (IBSIModelListener listener : listeners) {
			listener.childChanged(category, child);
		}
	}
	
	public void refreshAllListeners() {
		for (IBSIModelListener listener : listeners) {
			listener.modelRefresh();
		}
	}
	
	public void addBSIModelListener(IBSIModelListener listener) {
		if (! listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeBSIModelListener(IBSIModelListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Set<ITVerbund> getItverbuende() {
		Set itvs = new HashSet<ITVerbund>();
		itvs.addAll(getChildren());
		return itvs;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof ITVerbund)
			return true;
		return false;
	}
	
	
	public ArrayList<BausteinUmsetzung> getBausteine() {
		ArrayList<BausteinUmsetzung> result = new ArrayList<BausteinUmsetzung>();
		Set<CnATreeElement> verbuende = getChildren();
		for (CnATreeElement verbund : verbuende) {
			getBausteine(verbund, result);
		}
		return result;
	}
	
	public ArrayList<MassnahmenUmsetzung> getMassnahmen() {
		ArrayList<MassnahmenUmsetzung> result = new ArrayList<MassnahmenUmsetzung>();
		Set<CnATreeElement> verbuende = getChildren();
		for (CnATreeElement verbund : verbuende) {
			getMassnahmen(verbund, result);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void getBausteine(CnATreeElement elmnt, 
			ArrayList<BausteinUmsetzung> result ) {
		
		for (CnATreeElement elmt : elmnt.getChildren()) {
			if (elmt instanceof BausteinUmsetzung) {
				result.add((BausteinUmsetzung) elmt);
			}
			else {
				getBausteine(elmt, result);
			}
		}
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void getMassnahmen(CnATreeElement elmnt, 
			ArrayList<MassnahmenUmsetzung> result ) {
		
		for (CnATreeElement elmt : elmnt.getChildren()) {
			if (elmt instanceof BausteinUmsetzung) {
				Set<CnATreeElement> massnahmen = elmt.getChildren();
				for (CnATreeElement obj : massnahmen) {
					if (obj instanceof MassnahmenUmsetzung)
						result.add((MassnahmenUmsetzung)obj);
				}
			}
			else {
				getMassnahmen(elmt, result);
			}
		}
	}

	public double getDbVersion() {
		return dbVersion != null ? dbVersion : 0D;
	}

	public void setDbVersion(Double dbVersion) {
		this.dbVersion = dbVersion;
	}

	public ArrayList<Person> getPersonen() {
		ArrayList<Person> result = new ArrayList<Person>(50);
		Set<CnATreeElement> verbuende = getChildren();
		for (CnATreeElement verbund : verbuende) {
			for (CnATreeElement kategorie : verbund.getChildren()) {
				if (kategorie instanceof PersonenKategorie) {
					Set<CnATreeElement> personen = kategorie.getChildren();
					for (Iterator iter = personen.iterator(); iter.hasNext();) {
						result.add( (Person) iter.next());
					}
				}
				
			}
		}
		return result;
	}

	

}
