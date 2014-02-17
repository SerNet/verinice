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
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Creates a list of parameters for new  GSM processes.
 *
 * This class creates one process for each person and control group which are
 * connected. 
 * 
 * Configured in veriniceserver-jbpm.xml.
 * Used by {@link sernet.verinice.bpm.gsm.GsmService}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessCreaterForPersons extends GsmProcessParameterCreater {

    private static final Logger LOG = Logger.getLogger(ProcessCreaterForPersons.class);
    
    @Override
    protected List<CnATreeElement> getLeftHandElements(Integer orgId) {
        return getPersons();
    }
    
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> getPersons() {
        DetachedCriteria crit = createDefaultCriteria();
        crit.add(Restrictions.eq("objectType", PersonIso.TYPE_ID));
        return getElementDao().findByCriteria(crit);
    }
    
    @Override
    protected Set<CnATreeElement> getObjectsForLeftElement(CnATreeElement leftElement) {
        return getObjectsForPerson(leftElement);
    }
    
    private Set<CnATreeElement> getObjectsForPerson(CnATreeElement person) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading objects for person: " + person.getTitle());
        }
        // elements is a set of AssetGroups
        Set<CnATreeElement> elements = getGraph().getLinkTargets(person,AssetGroup.REL_PERSON_ISO);
        Set<CnATreeElement> assets = new HashSet<CnATreeElement>();
        for (CnATreeElement assetGroup : elements) {
            assets.addAll(getGraph().getLinkTargets(assetGroup, Edge.RELATIVES));
        }
        Set<CnATreeElement> scenarios = new HashSet<CnATreeElement>();
        for (CnATreeElement asset : assets) {
            scenarios.addAll(getGraph().getLinkTargets(asset, IncidentScenario.REL_INCSCEN_ASSET));
        }
        Set<CnATreeElement> controls = new HashSet<CnATreeElement>();
        for (CnATreeElement scen : scenarios) {
            controls.addAll(getGraph().getLinkTargets(scen, Control.REL_CONTROL_INCSCEN));
        }
        elements.addAll(assets);
        elements.addAll(scenarios);
        elements.addAll(controls);
        return elements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.gsm.GsmProcessParameterCreater#getPersonForLeftElement(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    protected Set<CnATreeElement> getPersonForLeftElement(CnATreeElement person) {
        Set<CnATreeElement> personSet = new HashSet<CnATreeElement>();
        personSet.add(person);
        return personSet;
    }
}
