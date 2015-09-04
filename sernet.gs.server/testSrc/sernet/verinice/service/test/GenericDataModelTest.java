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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.bpm.ProzessExecution;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.report.service.impl.dynamictable.GenericDataModel;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;
import sernet.verinice.service.commands.LoadCnAElementByExternalID;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.commands.UpdateElementEntity;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GenericDataModelTest extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(GenericDataModelTest.class);
    
    private static final String VNA_FILENAME = "GenericDataModelTest.vna";
    
    private static final String SOURCE_ID = "dm-2015-06-22";
    private static final String EXT_ID_ORG = "ENTITY_15063";
    private static final String EXT_ID_PROCESS_GROUP = "ENTITY_24086";
    private static final String EXT_ID_CONTROL_GROUP = "ENTITY_17875";
    

    
    @Resource(name="cnaTreeElementDao")
    protected IBaseDao<CnATreeElement, Long> elementDao;
    
    @Test
    public void testChildParentReport() throws CommandException {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        
        VeriniceGraph graph = createGraph(
                org,
                new String[]{Asset.TYPE_ID,AssetGroup.TYPE_ID},
                new String[]{});
        
        GenericDataModel dm = new GenericDataModel(graph, new String[]{
                "asset<assetgroup.assetgroup_name",
                "asset.asset_name"});
        dm.init();
        List<List<String>> resultTable = dm.getResult();
        
        List<String> assetNames = new LinkedList<String>();
        for (List<String> row : resultTable) {
            assetNames.add(row.get(1));
        }
        
        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(Asset.TYPE_ID,org.getDbId());
        command = commandService.executeCommand(command);
        List<CnATreeElement> assetList = command.getElements();
        
        assertEquals("Result table has not " + assetList + " rows", assetList.size(), resultTable.size());
        
        for (CnATreeElement asset : assetList) {
            assertTrue("Asset: " + asset.getTitle() + " not in result list",assetNames.contains(asset.getTitle()));
        }
    }
    
    @Test
    public void testParentChildReport() throws CommandException {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
    
        VeriniceGraph graph = createGraph(
                org,
                new String[]{ProcessGroup.TYPE_ID,Process.TYPE_ID},
                new String[]{});
        
        GenericDataModel dm = new GenericDataModel(graph, new String[]{
                "process_group.process_group_name",
                "process_group>process.process_name"});
        dm.init();
        List<List<String>> resultTable = dm.getResult();
        assertEquals("Result table has not 7 rows", 7, resultTable.size());
        
        List<String> processNames = new LinkedList<String>();
        for (List<String> row : resultTable) {
            processNames.add(row.get(1));
        }
        
        CnATreeElement processGroup = loadGroupWithChildren(EXT_ID_PROCESS_GROUP);
        Set<CnATreeElement> processSet = processGroup.getChildren();
        for (CnATreeElement process : processSet) {
            assertTrue("Process: " + process.getTitle() + " not in result list",processNames.contains(process.getTitle()));
        }
    }
    
    @Test
    public void testLinkReport() throws CommandException {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        VeriniceGraph graph = createGraph(
                org,
                new String[]{Audit.TYPE_ID,ControlGroup.TYPE_ID},
                new String[]{Audit.REL_AUDIT_CONTROLGROUP,Audit.REL_AUDIT_CONTROL});
        
        GenericDataModel dm = new GenericDataModel(graph, new String[]{
                "audit.audit_name",
                "audit/controlgroup.controlgroup_name"});
        dm.init();
        List<List<String>> resultTable = dm.getResult();
        assertEquals("Result table has not 12 rows", 12, resultTable.size());
        
        List<String> controlGroupNames = new LinkedList<String>();
        for (List<String> row : resultTable) {
            controlGroupNames.add(row.get(1));
        }
        
        CnATreeElement controlGroup = loadGroupWithChildren(EXT_ID_CONTROL_GROUP);
        Set<CnATreeElement> controlGroupSet = controlGroup.getChildren();
        for (CnATreeElement group : controlGroupSet) {
            if(group instanceof ControlGroup) {
                assertTrue("Process: " + group.getTitle() + " not in result list",controlGroupNames.contains(group.getTitle()));      
            }
        }      
    }

    private VeriniceGraph createGraph(CnATreeElement org, String[] typeIds, String[] relationIds) throws CommandException {
        GraphCommand command = new GraphCommand();
        GraphElementLoader loader = new GraphElementLoader();
        loader.setScopeId(org.getDbId());
        loader.setTypeIds(typeIds);
        command.addLoader(loader);
        for (String relationId : relationIds) {
            command.addRelationId(relationId);
        }

        command = commandService.executeCommand(command);          
        VeriniceGraph graph = command.getGraph();
        return graph;
    }

    private CnATreeElement loadGroupWithChildren(String extIdGroup) throws CommandException {
        CnATreeElement processGroup = loadElement(SOURCE_ID, extIdGroup);
        RetrieveInfo ri = RetrieveInfo.getChildrenInstance();
        ri.setChildrenProperties(true);
        LoadElementByUuid<CnATreeElement> loadCommand = new LoadElementByUuid<>(processGroup.getUuid(), ri);
        loadCommand = commandService.executeCommand(loadCommand);  
        processGroup = loadCommand.getElement();
        return processGroup;
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
