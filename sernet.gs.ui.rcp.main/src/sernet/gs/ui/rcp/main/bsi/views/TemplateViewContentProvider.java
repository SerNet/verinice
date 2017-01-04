/*******************************************************************************  
 * Copyright (c) 2016 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.validation.CnAValidation;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */
public class TemplateViewContentProvider implements IStructuredContentProvider, IBSIModelListener {

    private TemplateView templateView;
    private TableViewer table;

    public TemplateViewContentProvider(TemplateView view, TableViewer viewer) {
        this.templateView = view;
        this.table = viewer;
    }

    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInputElement) {
        if (newInputElement instanceof PlaceHolder) {
            return;
        }
        CnATreeElement inputElement = (CnATreeElement) newInputElement;
        templateView.setInputElement(inputElement);
        table.refresh();
    }

    @Override
    public Object[] getElements(Object obj) {
        if (obj instanceof PlaceHolder) {
            return new Object[] { obj };
        }
        if (templateView == null || templateView.getInputElement() == null) {
            return new Object[] {};
        }

        return templateView.getTemplates().toArray();
    }

    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#childAdded(sernet.verinice.
     * model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#childRemoved(sernet.verinice.
     * model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#childChanged(sernet.verinice.
     * model.common.CnATreeElement)
     */
    @Override
    public void childChanged(CnATreeElement child) {
        // reload because a title may have changed
        templateView.reloadAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#linkChanged(sernet.verinice.
     * model.common.CnALink, sernet.verinice.model.common.CnALink,
     * java.lang.Object)
     */
    @Override
    public void linkChanged(CnALink oldLink, CnALink newLink, Object source) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#linkRemoved(sernet.verinice.
     * model.common.CnALink)
     */
    @Override
    public void linkRemoved(CnALink link) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#linkAdded(sernet.verinice.
     * model.common.CnALink)
     */
    @Override
    public void linkAdded(CnALink link) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#databaseChildAdded(sernet.
     * verinice.model.common.CnATreeElement)
     */
    @Override
    public void databaseChildAdded(CnATreeElement child) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#databaseChildRemoved(sernet.
     * verinice.model.common.CnATreeElement)
     */
    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        templateView.reloadAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#databaseChildRemoved(sernet.
     * verinice.model.common.ChangeLogEntry)
     */
    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#databaseChildChanged(sernet.
     * verinice.model.common.CnATreeElement)
     */
    @Override
    public void databaseChildChanged(CnATreeElement child) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#modelReload(sernet.verinice.
     * model.bsi.BSIModel)
     */
    @Override
    public void modelReload(BSIModel newModel) {
        templateView.reloadAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh()
     */
    @Override
    public void modelRefresh() {
        templateView.reloadAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh(java.lang.
     * Object)
     */
    @Override
    public void modelRefresh(Object source) {
        templateView.reloadAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#validationAdded(java.lang.
     * Integer)
     */
    @Override
    public void validationAdded(Integer scopeId) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#validationRemoved(java.lang.
     * Integer)
     */
    @Override
    public void validationRemoved(Integer scopeId) {
        // only react to element changes
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#validationChanged(sernet.
     * verinice.model.validation.CnAValidation,
     * sernet.verinice.model.validation.CnAValidation)
     */
    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation) {
        // only react to element changes
    }
}
