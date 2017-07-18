/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.service.test;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphServiceTest extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(GraphServiceTest.class);
    
    private static final String VNA_FILENAME = "GraphServiceTest.vna";
    
    private static final String SOURCE_ID = "dm-2015-06-04";
    private static final String EXT_ID_ORG = "ENTITY_15063";
    private static final String THEFT_OF_MEDIA_TITLE = "Theft of media by unauthorized persons";
    private static final String WIRETAPPING_TITLE = "Wiretapping";
    private static final String DISRUPTION_TITLE = "Disruption of network operation in routing";
    @Resource(name="graphService")
    IGraphService graphService;
    
    @Resource(name="cnaTreeElementDao")
    protected IBaseDao<CnATreeElement, Long> elementDao;
    
    @Test
    public void testLoadNode() throws Exception {
        List<String> uuidListAsset = getUuidsOfType(Asset.TYPE_ID);    
        List<String> uuidListControls = getUuidsOfType(Control.TYPE_ID);
        List<String> uuidListIncidentScenario = getUuidsOfType(IncidentScenario.TYPE_ID);
        
        VeriniceGraph graph = createGraph(
                null, 
                new String[]{Asset.TYPE_ID,Control.TYPE_ID,IncidentScenario.TYPE_ID}, 
                null);
        
        checkElements(uuidListAsset, graph.getElements(Asset.TYPE_ID));
        checkElements(uuidListControls, graph.getElements(Control.TYPE_ID));
        checkElements(uuidListIncidentScenario, graph.getElements(IncidentScenario.TYPE_ID));
    }
    
    @Test
    public void testLoadAllRelations() throws Exception {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        VeriniceGraph graph = createGraph(
                org.getDbId(),
                new String[]{Asset.TYPE_ID,Control.TYPE_ID,IncidentScenario.TYPE_ID, Vulnerability.TYPE_ID, Threat.TYPE_ID},
                null);
        Set<CnATreeElement> isList = graph.getElements(IncidentScenario.TYPE_ID);
        
        IncidentScenario is = findByTitle(isList, THEFT_OF_MEDIA_TITLE);
        Set<CnATreeElement> targets = graph.getLinkTargets(is);
        assertTrue("Number od links targets is not 8", targets.size()==8);
        targets = graph.getLinkTargetsByElementType(is, Asset.TYPE_ID);
        assertTrue("Number od links targets is not 3", targets.size()==3);
        targets = graph.getLinkTargetsByElementType(is, Control.TYPE_ID);
        assertTrue("Number od links targets is not 3", targets.size()==3);
        targets = graph.getLinkTargetsByElementType(is, Threat.TYPE_ID);
        assertTrue("Number od links targets is not 1", targets.size()==1);
        targets = graph.getLinkTargetsByElementType(is, Vulnerability.TYPE_ID);
        assertTrue("Number od links targets is not 1", targets.size()==1);
        
        is = findByTitle(isList, WIRETAPPING_TITLE);
        targets = graph.getLinkTargets(is);
        assertTrue("Number od links targets is not 14", targets.size()==14);
        targets = graph.getLinkTargets(is, IncidentScenario.REL_INCSCEN_ASSET);
        assertTrue("Number od links targets is not 9", targets.size()==9);
        targets = graph.getLinkTargets(is, Control.REL_CONTROL_INCSCEN);
        assertTrue("Number od links targets is not 3", targets.size()==3);
        targets = graph.getLinkTargets(is, IncidentScenario.REL_INCSCEN_VULNERABILITY);
        assertTrue("Number od links targets is not 3", targets.size()==1);
        targets = graph.getLinkTargets(is, IncidentScenario.REL_INCSCEN_THREAT);
        assertTrue("Number od links targets is not 3", targets.size()==1);
    }
    
    @Test
    public void testLoadRelations() throws Exception {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        VeriniceGraph graph = createGraph(
                org.getDbId(),
                new String[]{Asset.TYPE_ID,Control.TYPE_ID,IncidentScenario.TYPE_ID, Vulnerability.TYPE_ID, Threat.TYPE_ID},
                new String[]{IncidentScenario.REL_INCSCEN_ASSET,Control.REL_CONTROL_INCSCEN});
        Set<CnATreeElement> isList = graph.getElements(IncidentScenario.TYPE_ID);
        IncidentScenario is = findByTitle(isList, DISRUPTION_TITLE);
        
        Set<CnATreeElement> targets = graph.getLinkTargets(is, IncidentScenario.REL_INCSCEN_ASSET);
        assertTrue("Number od links targets is not 10", targets.size()==10);
        targets = graph.getLinkTargets(is, Control.REL_CONTROL_INCSCEN);
        assertTrue("Number od links targets is not 3", targets.size()==3);
    }

    private VeriniceGraph createGraph(Integer scopeId, String[] typeIds, String[] relationsTypeIds) throws CommandException {      
        IGraphElementLoader loader = new GraphElementLoader();
        if(typeIds!=null) {
            loader.setTypeIds(typeIds);
        }
        if(scopeId!=null) {
            loader.setScopeId(scopeId);
        }
        loader.setCnaTreeElementDao(elementDao);
        graphService.setLoader(loader);  
        if(relationsTypeIds!=null) {
            graphService.setRelationIds(relationsTypeIds);
        }
        graphService.create();
        VeriniceGraph graph = graphService.getGraph();
        return graph;
    }

    private IncidentScenario findByTitle(Set<CnATreeElement> isList, String title) {
        IncidentScenario theftOfMedia = null;
        for (CnATreeElement element : isList) {
            if(title.equals(element.getTitle())) {
                theftOfMedia = (IncidentScenario) element;
                break;
            }
        }
        return theftOfMedia;
    }

    private void checkElements(List<String> uuidListAsset, Set<CnATreeElement> elementSet) {
        for (CnATreeElement element : elementSet) {
            uuidListAsset.remove(element.getUuid());
        }
        assertTrue("uuidList is not empty",uuidListAsset.isEmpty());
    }
    
    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
       return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
