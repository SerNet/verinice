/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.catalog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.common.CnATreeElement;

/**
 * The root {@link CnATreeElement} for all catalogs displayed by the
 * CatalogView.
 * 
 * A catalog consists of any elements. All elements in a catalog are immutable.
 * Elements in a catalog are templates for the elements in other views.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class CatalogModel extends CnATreeElement {
    private static final Logger LOG = Logger.getLogger(CatalogModel.class);
    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "catalog_model";
    private transient List<ICatalogModelListener> listeners;

    private Lock lock = new ReentrantLock();

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTitle() {
        return "Catalog Model";
    }

    @Override
    public void refreshAllListeners(Object source) {
        for (ICatalogModelListener listener : getListeners()) {
            listener.modelRefresh(source);
        }
    }

    public void addCatalogModelListener(ICatalogModelListener listener) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding Catalog model listener.");
        }
        if (!getListeners().contains(listener)) {
            getListeners().add(listener);
        }
    }

    public void removeCatalogModelListener(ICatalogModelListener listener) {
        if (getListeners().contains(listener)) {
            getListeners().remove(listener);
        }
    }

    public void modelReload(CatalogModel newModel) {
        for (ICatalogModelListener listener : getListeners()) {
             listener.modelReload(newModel);
            if (LOG.isDebugEnabled()) {
                LOG.debug("modelReload, listener: " + listener); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        for (ICatalogModelListener listener : getListeners()) {
            listener.databaseChildRemoved(child);
        }
    }

    /**
     * Create the listener list lazy. Ensure the list is created only once and synchronize only the necessary part of the code.
     */
    public List<ICatalogModelListener> getListeners() {
        if (listeners == null) {
            lock.lock();
            try {
                if (listeners != null) {
                    return listeners;
                }
                listeners = new CopyOnWriteArrayList<>();
            } finally {
                lock.unlock();
            }
        }
        return listeners;
    }

    /**
     * Moves all {@link IBpModelListener} from this model
     * to newModel.
     * 
     * @param newModel 
     */
    public void moveListener(CatalogModel newModel) {
        for (ICatalogModelListener listener : getListeners()) {
            newModel.addCatalogModelListener(listener);
        }
        for (ICatalogModelListener listener : getListeners()) {
            removeCatalogModelListener(listener);
        }      
    }    
}
