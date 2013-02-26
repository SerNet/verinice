/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.validation.CnAValidation;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class ISO27KModel extends CnATreeElement implements IISO27kRoot {

	private transient Logger log;
	
	public static final String TYPE_ID = "iso27kmodel"; //$NON-NLS-1$
	
	public static final String TITLE = "ISO 27000 Modeling"; //$NON-NLS-1$
	
	private transient List<IISO27KModelListener> listeners;
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return TITLE;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return (obj instanceof Organization || obj instanceof ImportIsoGroup);
	}

	@Override
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.childAdded(category, child);
			if (child instanceof Organization || child instanceof ImportIsoGroup) {
				listener.modelRefresh(null);
			}
		}
	}
	
	@Override
	public void databaseChildAdded(CnATreeElement child) {
		if (child == null){
			return;
		}
		for (IISO27KModelListener listener : getListeners()) {
			listener.databaseChildAdded(child);
		}
	}
	
	@Override
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.childRemoved(category, child);
		}
	}
	
	@Override
	public void removeChild(CnATreeElement child) {
		if (getChildren().remove(child)) {
			this.childRemoved(this, child);
		}
	}
	
	public void databaseChildRemoved(CnATreeElement child) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.databaseChildRemoved(child);
		}	
	}
	
	@Override
	public void databaseChildRemoved(ChangeLogEntry entry) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.databaseChildRemoved(entry);
		}
	}

	
	@Override
	public void childChanged(CnATreeElement child) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.childChanged(child);
		}
	}
	
	@Override
	public void databaseChildChanged(CnATreeElement child) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.databaseChildChanged(child);
		}
	}
	
	@Override
	public void linkChanged(CnALink old, CnALink link, Object source) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.linkChanged(old, link, source);
		}
	}
	
	@Override
	public void linkRemoved(CnALink link) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.linkRemoved(link);
		}
	}
	
	public void linkAdded(CnALink link) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.linkAdded(link);
		}
	}
	
	public void modelReload(ISO27KModel newModel) {
		for (IISO27KModelListener listener : getListeners()) {
			listener.modelReload(newModel);
			if (getLog().isDebugEnabled()) {
				getLog().debug("modelReload, listener: " + listener); //$NON-NLS-1$
			}
		}
	}
	
	public void addISO27KModelListener(IISO27KModelListener listener) {
	    if (getLog().isDebugEnabled()) {
            getLog().debug("Adding ISO model listener.");
        }
		if (!getListeners().contains(listener)){
			getListeners().add(listener);
		}
	}
	
	public void removeISO27KModelListener(IISO27KModelListener listener) {
		if (getListeners().contains(listener)){
			getListeners().remove(listener);
		}
	}
	
	private synchronized List<IISO27KModelListener> getListeners() {
		if (listeners == null){
			listeners = new CopyOnWriteArrayList<IISO27KModelListener>();
		}
		return listeners;
	}
	
	@Override
	public void refreshAllListeners(Object source) {
		Logger.getLogger(this.getClass()).debug("Model refresh to all listeners."); //$NON-NLS-1$
		for (IISO27KModelListener listener : getListeners()) {
			listener.modelRefresh(source);
		}
	}
	
	private Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(ISO27KModel.class);
		}
		return log;
	}

    /**
     * Moves all {@link IISO27KModelListener} from this model
     * to newModel.
     * 
     * @param newModel 
     */
    public void moveListener(ISO27KModel newModel) {
        for (IISO27KModelListener listener : getListeners()) {
            newModel.addISO27KModelListener(listener);
        }
        for (IISO27KModelListener listener : getListeners()) {
            removeISO27KModelListener(listener);
        }      
    }
    
    public void validationAdded(Integer scopeId){
        for(IISO27KModelListener listener : getListeners()){
            listener.validationAdded(scopeId);
        }
    };
    
    public void validationRemoved(Integer scopeId){
        for(IISO27KModelListener listener : getListeners()){
            listener.validationRemoved(scopeId);
        }
    };
    
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){
        // do nothing
    };
}
