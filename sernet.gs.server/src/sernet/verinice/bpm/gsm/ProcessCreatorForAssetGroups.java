/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.gsm;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * Creates a list of parameters for new  GSM processes.
 *
 * This class creates one process for each asset group and control group which are
 * connected. 
 * 
 * Configured in veriniceserver-jbpm.xml.
 * Used by {@link sernet.verinice.bpm.gsm.GsmService}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessCreatorForAssetGroups extends GsmProcessParameterCreater {
    
    private static final Logger LOG = Logger.getLogger(ProcessCreatorForAssetGroups.class);
    
    @Override
    protected List<CnATreeElement> getLeftHandElements(Integer orgId) {
        return get2ndLevelAssetGroups(orgId);
    }
    
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> get2ndLevelAssetGroups(Integer orgId) {
        StringBuffer hql = new StringBuffer("select e.dbId from CnATreeElement e "); //$NON-NLS-1$      
        hql.append("where e.objectType = ? and e.parent.parent.dbId = ?"); //$NON-NLS-1$ 
        List<Integer> dbIdResult = getElementDao().findByQuery(hql.toString(),new Object[]{AssetGroup.TYPE_ID,orgId});
        if(dbIdResult==null || dbIdResult.isEmpty()) {
            return Collections.emptyList();
        }
        DetachedCriteria crit = createDefaultCriteria();
        Integer[] dbIdArray = dbIdResult.toArray(new Integer[dbIdResult.size()]);
        crit.add(Restrictions.in("dbId", dbIdArray));
        return getElementDao().findByCriteria(crit);   
    }
    
    
    
    @Override
    protected Set<CnATreeElement> getObjectsForLeftElement(CnATreeElement assetGroup) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading objects for asset group: " + assetGroup.getTitle());
        }     
        Set<CnATreeElement> elements = new HashSet<CnATreeElement>();
        elements.add(assetGroup);
        
        elements.addAll(getGraph().getLinkTargets(assetGroup, Edge.RELATIVES));
        
        Set<CnATreeElement> scenarios = new HashSet<CnATreeElement>();
        for (CnATreeElement asset : elements) {
            scenarios.addAll(getGraph().getLinkTargets(asset, IncidentScenario.REL_INCSCEN_ASSET));
        }
        Set<CnATreeElement> controls = new HashSet<CnATreeElement>();
        for (CnATreeElement scen : scenarios) {
            controls.addAll(getGraph().getLinkTargets(scen, Control.REL_CONTROL_INCSCEN));
        }
        elements.addAll(scenarios);
        elements.addAll(controls);
        return elements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.gsm.GsmProcessParameterCreater#getPersonForLeftElement(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    protected Set<CnATreeElement> getPersonForLeftElement(CnATreeElement assetGroup) {
        return getGraph().getLinkTargets(assetGroup, AssetGroup.REL_PERSON_ISO);       
    }

}
