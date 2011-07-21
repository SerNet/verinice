/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bsi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IVersionConstants;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;

/**
 * Top level category for all BSI model elements.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class BSIModel extends CnATreeElement implements IBSIStrukturElement {

	public static final String TYPE_ID = "bsimodel"; //$NON-NLS-1$

	/**
	 * DB version of this BSI model.
	 * 
	 * Will initially be set to DbVersion.COMPATIBLE_DB_VERSION and updated by migration
	 * commands when needed.
	 */
	private Double dbVersion = 0D;

	private transient List<IBSIModelListener> listeners;


	public BSIModel() {
		super(null);
		// current version number for new models, wil be saved in database:
		
		dbVersion = IVersionConstants.COMPATIBLE_DB_VERSION;
	}

	public String getKuerzel() {
		return ""; //$NON-NLS-1$
	}

	public int getSchicht() {
		return 1;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public String getTitle() {
		return Messages.BSIModel_0;
	}

	@Override
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		for (IBSIModelListener listener : getListeners()) {
			listener.childAdded(category, child);
			if (child instanceof ITVerbund | child instanceof ImportBsiGroup)
				listener.modelRefresh(null);
		}
	}

	private synchronized List<IBSIModelListener> getListeners() {
		if (listeners == null)
			listeners = new CopyOnWriteArrayList<IBSIModelListener>();
		return listeners;
	}

	@Override
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		for (IBSIModelListener listener : getListeners()) {
			listener.childRemoved(category, child);
		}
	}

	@Override
	public void childChanged(CnATreeElement category, CnATreeElement child) {
		for (IBSIModelListener listener : getListeners()) {
			listener.childChanged(category, child);
		}
	}

	@Override
	public void linkChanged(CnALink old, CnALink link, Object source) {
		for (IBSIModelListener listener : getListeners()) {
			listener.linkChanged(old, link, source);
		}
	}
	
	@Override
	public void linkRemoved(CnALink link) {
		for (IBSIModelListener listener : getListeners()) {
			listener.linkRemoved(link);
		}
	}
	
	public void linkAdded(CnALink link) {
		for (IBSIModelListener listener : getListeners()) {
			listener.linkAdded(link);
		}
	}

	@Override
	public void refreshAllListeners(Object source) {
		Logger.getLogger(this.getClass()).debug(Messages.BSIModel_1);
		for (IBSIModelListener listener : getListeners()) {
			listener.modelRefresh(source);
		}
	}
	
	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	@Override
	public void modelRefresh() {
		modelRefresh(null);
	}

	@Override
	public void modelRefresh(Object source) {
		refreshAllListeners(source);
	}
	
	@Override
	public void modelReload(BSIModel newModel) {
		for (IBSIModelListener listener : getListeners()) {
			listener.modelReload(newModel);
		}
	}

	public void addBSIModelListener(IBSIModelListener listener) {
		if (!getListeners().contains(listener))
			getListeners().add(listener);
	}

	public void removeBSIModelListener(IBSIModelListener listener) {
		if (getListeners().contains(listener))
			getListeners().remove(listener);
	}

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public List<ITVerbund> getItverbuende() {
		List itvs = new ArrayList<ITVerbund>();
		for (CnATreeElement c : getChildren())
			if (c.getTypeId() == ITVerbund.TYPE_ID)
				itvs.add(c);
		
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
		if (obj instanceof ITVerbund || obj instanceof ImportBsiGroup)
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

	private void getBausteine(CnATreeElement elmnt,
			ArrayList<BausteinUmsetzung> result) {

		try {
//			if (elmnt.getChildren() instanceof PersistentCollection) {
//				PersistentCollection collection = (PersistentCollection) elmnt.getChildren();
//				if (!collection.wasInitialized())
//					return;
//			}
			
			for (CnATreeElement elmt : elmnt.getChildren()) {
				if (elmt instanceof BausteinUmsetzung) {
					BausteinUmsetzung bst = (BausteinUmsetzung) elmt;
					result.add(bst);
				} else {
					getBausteine(elmt, result);
				}
			}
		}
		catch (Exception e) {
			// DONE  implement dialog for consolidator that loads and displays necessary bausteine 
			// FIXME consolidator does not update changed elements in GUI, user has to click reload
		}
	}

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

	public List<CnATreeElement> getAllElementsFlatList(boolean includeMassnahmen) {
		List<CnATreeElement> result = new ArrayList<CnATreeElement>();

		for (CnATreeElement child : getChildren()) {
				if (!includeMassnahmen &&  child instanceof BausteinUmsetzung) {
					// do not add massnahmen and bausteine
				}
				else {
					result.add(child);
//					Logger.getLogger(this.getClass()).debug("Adding " + child);
					findChildren(result, child, includeMassnahmen);
				}
			}
		return result;
	}

	private void findChildren(List<CnATreeElement> result, CnATreeElement parent, boolean includeMassnahmen) {
		if (!includeMassnahmen && parent instanceof BausteinUmsetzung)
			return;
		
		Set<CnATreeElement> children = parent.getChildren();
		if (children != null && children.size() > 0) {
//			Logger.getLogger(this.getClass()).debug("Adding " + children.toString());
			result.addAll(children);
			for (CnATreeElement child : children) {
				findChildren(result, child, includeMassnahmen);
			}
		}
	}

	public List<CnALink> getAllLinks() {
		List<CnALink> result = new ArrayList<CnALink>();
		for (CnATreeElement element : getAllElementsFlatList(false /* do not load Massnahmen */)) {
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

	@Override
	public void databaseChildAdded(CnATreeElement child) {
		if (child == null)
			return;
		
		for (IBSIModelListener listener : getListeners()) {
			listener.databaseChildAdded(child);
		}
	}
	
	@Override
	public void databaseChildChanged(CnATreeElement child) {
		for (IBSIModelListener listener : getListeners()) {
			listener.databaseChildChanged(child);
		}
	}
	
	@Override
	public void databaseChildRemoved(CnATreeElement child) {
		for (IBSIModelListener listener : getListeners()) {
			listener.databaseChildRemoved(child);
		}
	}

	@Override
	public void databaseChildRemoved(ChangeLogEntry entry) {
		for (IBSIModelListener listener : getListeners()) {
			listener.databaseChildRemoved(entry);
		}
	}

	/**
     * Moves all {@link IISO27KModelListener} from this model
     * to newModel.
     * 
     * @param newModel 
     */
    public void moveListener(BSIModel newModel) {
        for (IBSIModelListener listener : getListeners()) {
            newModel.addBSIModelListener(listener);
        }
        for (IBSIModelListener listener : getListeners()) {
            removeBSIModelListener(listener);
        }      
    }
	
}
