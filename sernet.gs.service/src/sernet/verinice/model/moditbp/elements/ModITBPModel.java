/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.moditbp.elements;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.moditbp.IModITBPModelListener;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ModITBPModel extends CnATreeElement implements ModITBPRoot {
    
    private transient List<IModITBPModelListener> listeners;
    private transient Logger log;
    
    public static final String TYPE_ID = "moditbpmodel"; //$NON-NLS-1$
    
    public static final String TITLE = "Modernized ITBP Modeling"; //$NON-NLS-1$
    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTitle()
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
    public void refreshAllListeners(Object source) {
        Logger.getLogger(this.getClass()).debug("Model refresh to all listeners."); //$NON-NLS-1$
        for (IModITBPModelListener listener : getListeners()) {
            listener.modelRefresh(source);
        }
    }
    
    private synchronized List<IModITBPModelListener> getListeners() {
        if (listeners == null){
            listeners = new CopyOnWriteArrayList<IModITBPModelListener>();
        }
        return listeners;
    }
    
    @Override
    public boolean canContain(Object obj) {
        return (obj instanceof ITNetwork);
    }

    @Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.childAdded(category, child);
            if (child instanceof ITNetwork) {
                listener.modelRefresh(null);
            }
        }
    }
    
    @Override
    public void databaseChildAdded(CnATreeElement child) {
        if (child == null){
            return;
        }
        for (IModITBPModelListener listener : getListeners()) {
            listener.databaseChildAdded(child);
        }
    }
    
    @Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
        for (IModITBPModelListener listener : getListeners()) {
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
        for (IModITBPModelListener listener : getListeners()) {
            listener.databaseChildRemoved(child);
        }   
    }
    
    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.databaseChildRemoved(entry);
        }
    }

    
    @Override
    public void childChanged(CnATreeElement child) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.childChanged(child);
        }
    }
    
    @Override
    public void databaseChildChanged(CnATreeElement child) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.databaseChildChanged(child);
        }
    }
    
    @Override
    public void linkChanged(CnALink old, CnALink link, Object source) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.linkChanged(old, link, source);
        }
    }
    
    @Override
    public void linkRemoved(CnALink link) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.linkRemoved(link);
        }
    }
    
    public void linkAdded(CnALink link) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.linkAdded(link);
        }
    }
    
    public void modelReload(ModITBPModel newModel) {
        for (IModITBPModelListener listener : getListeners()) {
            listener.modelReload(newModel);
            if (getLog().isDebugEnabled()) {
                getLog().debug("modelReload, listener: " + listener); //$NON-NLS-1$
            }
        }
    }
    
    public void addModITBOModelListener(IModITBPModelListener listener) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Adding ISO model listener.");
        }
        if (!getListeners().contains(listener)){
            getListeners().add(listener);
        }
    }
    
    public void removeModITBPModelListener(IModITBPModelListener listener) {
        if (getListeners().contains(listener)){
            getListeners().remove(listener);
        }
    }
    
    /**
     * Moves all {@link IModITBPModelListener} from this model
     * to newModel.
     * 
     * @param newModel 
     */
    public void moveListener(ModITBPModel newModel) {
        for (IModITBPModelListener listener : getListeners()) {
            newModel.addModITBOModelListener(listener);
        }
        for (IModITBPModelListener listener : getListeners()) {
            removeModITBPModelListener(listener);
        }      
    }    
    
    private Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(ModITBPModel.class);
        }
        return log;
    }

}
