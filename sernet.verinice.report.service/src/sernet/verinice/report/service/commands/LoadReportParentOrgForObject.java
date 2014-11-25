/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * Find containing organization / scope starting from given element
 * by checking all parents recursively.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportParentOrgForObject extends GenericCommand implements ICachedCommand{

    private CnATreeElement child;
    private CnATreeElement org;

    private boolean resultInjectedFromCache = false;
    
    /**
     * @param audit
     */
    public LoadReportParentOrgForObject(CnATreeElement child) {
        this.child = child;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            if (child.getTypeId().equals(Organization.TYPE_ID)){
                org = child;
            } else {
                org = findOrg(child);
            }
        }
    }
    
    /**
     * @param cnATreeElement
     * @return
     */
    private CnATreeElement findOrg(CnATreeElement cnATreeElement) {
        CnATreeElement parent = cnATreeElement.getParent();
        if (parent == null){
            return null;
        }
        if (parent.getTypeId().equals(Organization.TYPE_ID)) {
            return parent;
        }
        return findOrg(parent);
    }

    /**
     * @return the org
     */
    public CnATreeElement getOrg() {
        return org;
    }
    
    public List<CnATreeElement> getElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>();
        result.add(org);
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(child.getUuid());
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.org = (CnATreeElement)result;
        resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.org;
    }
}


