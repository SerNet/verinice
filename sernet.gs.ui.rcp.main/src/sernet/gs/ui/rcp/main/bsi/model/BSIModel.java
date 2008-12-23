package sernet.gs.ui.rcp.main.bsi.model;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.DbVersion;

/**
 * Top level category for all BSI model elements.
 * 
 * @author koderman@sernet.de
 * 
 */
public class BSIModel extends CnATreeElement implements IBSIStrukturElement {

	private static final String TYPE_ID = "bsimodel"; //$NON-NLS-1$

	private Double dbVersion = 0D;

	private List<IBSIModelListener> listeners = new ArrayList<IBSIModelListener>();

	public BSIModel() {
		super(null);
		dbVersion = DbVersion.CURRENT_DB_VERSION;
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
			if (child instanceof ITVerbund)
				listener.modelRefresh();
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

	@Override
	public void linkChanged(CnALink link) {
		for (IBSIModelListener listener : listeners) {
			listener.linkChanged(link);
		}
	}

	public void refreshAllListeners() {
		for (IBSIModelListener listener : listeners) {
			listener.modelRefresh();
		}
	}

	public void addBSIModelListener(IBSIModelListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeBSIModelListener(IBSIModelListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public Set<ITVerbund> getItverbuende() {
		Set itvs = new HashSet<ITVerbund>();
		itvs.addAll(getChildren());
		return itvs;
	}

	@Override
	public void removeChild(CnATreeElement child) {
		if (getChildren().remove(child)) {
			this.childRemoved(this, child);
		}
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

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	private void getBausteine(CnATreeElement elmnt,
			ArrayList<BausteinUmsetzung> result) {

		for (CnATreeElement elmt : elmnt.getChildren()) {
			if (elmt instanceof BausteinUmsetzung) {
				result.add((BausteinUmsetzung) elmt);
			} else {
				getBausteine(elmt, result);
			}
		}
	}

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	private void getMassnahmen(CnATreeElement elmnt,
			ArrayList<MassnahmenUmsetzung> result) {

		for (CnATreeElement elmt : elmnt.getChildren()) {
			if (elmt instanceof BausteinUmsetzung
					|| elmt instanceof GefaehrdungsUmsetzung) {
				Set<CnATreeElement> massnahmen = elmt.getChildren();
				for (CnATreeElement obj : massnahmen) {
					if (obj instanceof MassnahmenUmsetzung)
						result.add((MassnahmenUmsetzung) obj);
				}
			} else {
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
						result.add((Person) iter.next());
					}
				}

			}
		}
		return result;
	}

	public List<CnATreeElement> getAllElements(boolean filterMassnahmen) {
		List<CnATreeElement> result = new ArrayList<CnATreeElement>();

		for (CnATreeElement child : getChildren()) {
				if (filterMassnahmen &&  child instanceof BausteinUmsetzung) {
					// do not add massnahmen and bausteine
				}
				else {
					result.add(child);
//					Logger.getLogger(this.getClass()).debug("Adding " + child);
					findChildren(result, child, filterMassnahmen);
				}
			}
		return result;
	}

	private void findChildren(List<CnATreeElement> result, CnATreeElement parent, boolean filterMassnahmen) {
		if (filterMassnahmen && parent instanceof BausteinUmsetzung)
			return;
		
		Set<CnATreeElement> children = parent.getChildren();
		if (children != null && children.size() > 0) {
//			Logger.getLogger(this.getClass()).debug("Adding " + children.toString());
			result.addAll(children);
			for (CnATreeElement child : children) {
				findChildren(result, child, filterMassnahmen);
			}
		}
	}

	public List<CnALink> getAllLinks() {
		List<CnALink> result = new ArrayList<CnALink>();
		for (CnATreeElement element : getAllElements(true /* do not load Massnahmen */)) {
			result.addAll(element.getLinksDown());
		}
		return result;
	}

	public List<String> getTags() {
		ArrayList<String> tags = new ArrayList<String>(50);
		Set<CnATreeElement> verbuende = getChildren();

		for (CnATreeElement verbund : verbuende) {
			for (CnATreeElement kategorie : verbund.getChildren()) {

				for (CnATreeElement zielobjekt : kategorie.getChildren()) {
					if (zielobjekt instanceof IBSIStrukturElement) {
						IBSIStrukturElement ziel = (IBSIStrukturElement) zielobjekt;
						tags.addAll(ziel.getTags());
					}
				}
			}
		}
		// remove doubles:
		HashSet<String> set = new HashSet<String>(tags);
		tags = new ArrayList<String>(set);

		Collections.sort(tags);
		return tags;
	}
}
